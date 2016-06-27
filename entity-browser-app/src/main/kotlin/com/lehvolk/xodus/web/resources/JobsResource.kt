package com.lehvolk.xodus.web.resources

import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam

@Path("/jobs")
class JobsResource : ApplicationResource() {

    @DELETE
    @Path("/type/{id}/entities")
    fun deleteEntities(
            @PathParam("id") id: Int,
            @QueryParam("q") term: String?) {
        log.debug("deleting entity for type {} and query {}", id, term)
        safely {
            jobsService.submit(storeService.deleteEntitiesJob(id, term))
        }
    }

}