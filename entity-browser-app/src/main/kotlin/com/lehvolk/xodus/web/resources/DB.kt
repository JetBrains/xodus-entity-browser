package com.lehvolk.xodus.web.resources

import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.db.DatabaseService
import mu.KLogging
import spark.kotlin.Http


class DB : Resource, ResourceSupport {

    companion object : KLogging() {
        private val databaseService = DatabaseService()
    }

    override val prefix = "/api/dbs/:uuid"

    override fun registerRouting(http: Http) {
        http.safeDelete(prefixed()) {
            databaseService.delete(db.uuid)
        }

        http.safePost(prefixed()) {
            val operation = request.queryParams("op")
            when (operation) {
                "start" -> databaseService.tryStart(db.uuid)
                "stop" -> databaseService.stop(db.uuid)
                else -> response.status(404)
            }
        }

        http.safeGet(prefixed("types")) {
            storeService.allTypes()
        }

        http.safePost<Any>(prefixed("types")) {
            val name = request.queryParams("name")
            storeService.addType(name)
        }

        http.safeDelete(prefixed("entities")) {
            val id = request.queryParams("id").toInt()
            val term = request.queryParams("term")
            jobsService.submit(storeService.deleteEntitiesJob(id, term))
        }
    }
}
