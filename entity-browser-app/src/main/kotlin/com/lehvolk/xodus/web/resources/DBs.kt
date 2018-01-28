package com.lehvolk.xodus.web.resources


import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.db.Databases
import spark.kotlin.Http

class DBs : Resource {

    override fun registerRouting(http: Http) {
        http.safeGet("/api/dbs") {
            Databases.all()
        }

        http.safePost<DBSummary>("/api/dbs") {
            Databases.add(it.location!!, it.key!!).also {
                Application.tryStart(it)
            }
        }
    }
}
