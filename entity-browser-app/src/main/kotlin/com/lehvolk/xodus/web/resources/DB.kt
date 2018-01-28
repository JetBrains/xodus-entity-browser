package com.lehvolk.xodus.web.resources

import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.db.Databases
import mu.KLogging
import spark.kotlin.Http


class DB : Resource, ResourceSupport {

    companion object : KLogging()

    override val prefix = "/api/dbs/:uuid"

    override fun registerRouting(http: Http) {
        http.safeDelete(prefixed()) {
            Application.stop(db)
            Databases.delete(db.uuid)
        }

        http.safePost<DBSummary>(prefixed()) {
            val operation = request.queryParams("op")
            when (operation) {
                "start" -> Application.tryStart(db)
                "stop" -> Application.stop(db)
            }
            db
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
