package com.lehvolk.xodus.web.resources

import com.lehvolk.xodus.web.DBSummary
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.QueryParam

class JobsResource(override val db: DBSummary) : DatabaseAwareResource() {

    @DELETE
    @Path("/entities")
    fun deleteEntities(
            @QueryParam("id") id: Int,
            @QueryParam("q") term: String?) {
        log.debug("deleting entity for type {} and query {}", id, term)
        safely {
            jobsService.submit(storeService.deleteEntitiesJob(id, term))
        }
    }

}