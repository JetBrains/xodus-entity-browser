package com.lehvolk.xodus.web.resources


import com.lehvolk.xodus.web.AppState
import com.lehvolk.xodus.web.DB
import com.lehvolk.xodus.web.DBSummary
import com.lehvolk.xodus.web.InjectionContexts
import com.lehvolk.xodus.web.db.Databases
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/db")
class DBResource : ApplicationResource() {

    @GET
    fun getAppSummary(): AppState {
        log.debug("getting database summary")
        safely {
            val db = Databases.current()
            return AppState().apply {
                recent = Databases.allRecent()
                opened = Databases.allOpened()
                if (db != null) {
                    current = DB().apply {
                        location = db.location
                        key = db.key
                        types = storeService.allTypes().sortedBy { it.name }
                    }
                }
            }
        }
    }

    @POST
    fun updateDB(db: DBSummary) {
        log.debug("update database summary")
        safely {
            InjectionContexts.start(db)
            response.switchTo(db.uuid)
        }
    }

    @DELETE
    fun deleteDB(db: DBSummary) {
        safely {
            Databases.delete(db)
        }
    }
}