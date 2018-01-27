package com.lehvolk.xodus.web.resources


import com.lehvolk.xodus.web.Application
import com.lehvolk.xodus.web.DBSummary
import com.lehvolk.xodus.web.db.Databases
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/dbs")
class DatabasesResource : ApplicationResource() {

    @GET
    fun getAll(): List<DBSummary> {
        log.debug("getting database summary")
        safely {
            return Databases.all()
        }
    }

    @POST
    fun newDB(db: DBSummary): DBSummary {
        log.debug("save new database")
        safely {
            return Databases.add(db.location!!, db.key!!).also {
                Application.tryStart(it)
            }
        }
    }

    @Path("/{uuid}")
    fun db(@PathParam("uuid") uuid: String): DatabaseResource {
        return DatabaseResource(Databases.find(uuid))
    }

    @Path("/{uuid}/jobs")
    fun dbJobs(@PathParam("uuid") uuid: String): JobsResource {
        return JobsResource(Databases.find(uuid))
    }
}