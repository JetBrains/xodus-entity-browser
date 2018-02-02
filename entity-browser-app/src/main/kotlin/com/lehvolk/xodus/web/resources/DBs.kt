package com.lehvolk.xodus.web.resources


import com.lehvolk.xodus.web.DBSummary
import com.lehvolk.xodus.web.Resource
import com.lehvolk.xodus.web.db.DatabaseService
import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.safeGet
import com.lehvolk.xodus.web.safePost
import spark.kotlin.Http

class DBs : Resource {

    private val databaseService = DatabaseService()

    override fun registerRouting(http: Http) {
        http.service.path("/api/dbs") {
            http.safeGet {
                Databases.all()
            }
            http.safePost<DBSummary> {
                databaseService.add(it.location, it.key, it.isOpened)
            }
        }
    }

}
