package com.lehvolk.xodus.web.resources


import com.lehvolk.xodus.web.DBSummary
import com.lehvolk.xodus.web.InjectionContexts
import com.lehvolk.xodus.web.db.Databases
import javax.ws.rs.*

@Path("/dbs")
class DatabasesResource : ApplicationResource() {

    @GET
    fun getAll(): List<DBSummary> {
        log.debug("getting database summary")
        safely {
            return Databases.allRecent()
        }
    }

    @POST
    fun updateDB(db: DBSummary) {
        log.debug("update database summary")
        safely {
            InjectionContexts.start(db)
        }
    }

    @DELETE
    fun deleteDB(db: DBSummary) {
        safely {
            Databases.delete(db)
        }
    }

    @Path("/{uuid}")
    fun db(@PathParam("uuid") uuid: String): DatabaseResource {
        val db = Databases.find(uuid) ?: throw NotFoundException()
        return DatabaseResource(db)
    }

    @Path("/{uuid}/jobs")
    fun dbJobs(@PathParam("uuid") uuid: String): JobsResource {
        val db = Databases.find(uuid) ?: throw NotFoundException()
        return JobsResource(db)
    }
}