package jetbrains.xodus.browser.web.resources

import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import jetbrains.xodus.browser.web.AppRoute
import jetbrains.xodus.browser.web.EntityType
import jetbrains.xodus.browser.web.db.DatabaseService
import mu.KLogging


class DB : AppRoute, ResourceSupport {

    companion object : KLogging() {
        private val databaseService = DatabaseService()
    }

    override fun Route.install() {
        route("/dbs/{uuid}") {
            delete {
                databaseService.delete(call.db.uuid)
            }
            post {
                val operation = call.request.queryParameters["op"]
                val result = when (operation) {
                    "start" -> {
                        databaseService.tryStart(call.db.uuid, false)
                    }
                    "stop" -> {
                        databaseService.stop(call.db.uuid)
                    }
                    else -> null
                }
                call.respond(result ?: HttpStatusCode.BadRequest)
            }
            get("/types") {
                call.respond(call.storeService.allTypes())
            }
            post("/types") {
                val type = call.receive(EntityType::class)
                call.storeService.addType(type.name)
                call.respond(call.storeService.allTypes())
            }
            delete("/entities") {
                val id = call.request.queryParameters["id"]?.toInt()
                        ?: throw BadRequestException("entity type required")
                val term = call.request.queryParameters["q"]
                call.jobsService.submit(call.storeService.deleteEntitiesJob(id, term))
            }
        }
    }
}
