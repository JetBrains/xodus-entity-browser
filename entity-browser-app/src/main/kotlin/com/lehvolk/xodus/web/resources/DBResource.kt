package com.lehvolk.xodus.web.resources


import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.db.Databases
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/db")
class DBResource : ApplicationResource() {

    @GET
    fun getDBSummary(): DBSummary {
        log.debug("getting database summary")
        safely {
            val db = Databases.current();
            return DBSummary().apply {
                location = db?.location
                key = db?.key
                if (db != null) {
                    types = storeService.allTypes().sortedBy { it.name }
                }
            }
        }
    }

    @POST
    fun updateDB(db: DB) {
        log.debug("update database summary")
        safely {
            InjectionContexts.start(db)
            response.switchTo(db.uuid)
        }
    }

    @DELETE
    fun deleteDB(db: DB) {
        safely {
            Databases.delete(db)
        }
    }
}