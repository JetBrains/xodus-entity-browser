package com.lehvolk.xodus.web.resources

import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.db.DatabaseService
import mu.KLogging
import spark.Spark
import spark.kotlin.Http


class DB : Resource, ResourceSupport {

    companion object : KLogging() {
        private val databaseService = DatabaseService()
    }

    override fun registerRouting(http: Http) {
        http.service.path("/api/dbs/:uuid") {
            http.safeDelete {
                databaseService.delete(db.uuid)
            }

            http.safePost {
                val operation = request.queryParams("op")
                when (operation) {
                    "start" -> databaseService.tryStart(db.uuid, false)
                    "stop" -> databaseService.stop(db.uuid)
                    else -> response.status(404)
                }
            }

            http.safeGet("/types") {
                storeService.allTypes()
            }

            http.safePost<EntityType>("/types") {
                storeService.addType(it.name)
                storeService.allTypes()
            }

            http.safeDelete("/entities") {
                val id = request.queryParams("id").toInt()
                val term = request.queryParams("q")
                jobsService.submit(storeService.deleteEntitiesJob(id, term))
            }
        }
    }
}
