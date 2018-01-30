package com.lehvolk.xodus.web.resources


import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.db.DatabaseService
import com.lehvolk.xodus.web.db.Databases
import spark.kotlin.Http

class DBs : Resource {

    private val databaseService = DatabaseService()

    override fun registerRouting(http: Http) {
        http.safeGet("/api/dbs") {
            Databases.all()
        }

        http.safePost<DBSummary>("/api/dbs") {
            databaseService.add(it.location, it.key, it.isOpened)
        }
    }

}
