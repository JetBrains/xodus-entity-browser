package jetbrains.xodus.browser.web.resources

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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