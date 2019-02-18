package jetbrains.xodus.browser.web.resources

import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import jetbrains.xodus.browser.web.AppRoute
import jetbrains.xodus.browser.web.EntityType
import jetbrains.xodus.browser.web.WebApplication
import mu.KLogging


class DB(webApp: WebApplication) : AppRoute, ResourceSupport(webApp) {

    companion object : KLogging()

    override fun Route.install() {
        route("/dbs/{uuid}") {
            delete {
                webApp.databaseService.delete(call.db.uuid)
                call.respond(HttpStatusCode.OK)
            }
            post {
                val operation = call.request.queryParameters["op"]
                val db = call.db
                val result = when (operation) {
                    "start" -> {
                        webApp.tryStartServices(db, false)
                        db
                    }
                    "stop" -> {
                        webApp.stop(db)
                        db
                    }
                    else -> null
                }
                call.respond(result?.let { webApp.databaseService.find(it.uuid) } ?: HttpStatusCode.BadRequest)
            }
            get("/types") {
                call.respond(call.storeService.allTypes())
            }
            post("/types") {
                call.assertEditable()
                val type = call.receive(EntityType::class)
                call.storeService.addType(type.name)
                call.respond(call.storeService.allTypes())
            }
            delete("/entities") {
                call.assertEditable()
                val id = call.request.queryParameters["id"]?.toInt()
                        ?: throw BadRequestException("entity type required")
                val term = call.request.queryParameters["q"]
                call.jobsService.submit(call.storeService.deleteEntitiesJob(id, term))
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}