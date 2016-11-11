package com.lehvolk.xodus.web.resources

import com.fasterxml.jackson.core.JsonProcessingException
import com.lehvolk.xodus.web.ChangeSummary
import com.lehvolk.xodus.web.EntityView
import com.lehvolk.xodus.web.SearchPager
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.StreamingOutput

@Path("/type")
class EntityTypeResource : ApplicationResource() {

    @GET
    @Path("{id}/entities")
    fun searchEntities(
            @PathParam("id") id: Int,
            @QueryParam("q") term: String?,
            @QueryParam("offset") offset: Int = 0,
            @QueryParam("pageSize") pageSize: Int = 0): SearchPager {
        log.debug("searching entities by typeId: {}, term [{}] with offset = {} and pageSize = {}",
                id, term, offset, pageSize)
        if (offset < 0 || pageSize < 0) {
            throw BadRequestException()
        }
        safely {
            return storeService.searchType(id, term, offset, if (pageSize == 0) 50 else Math.min(pageSize, 1000))
        }

    }

    @GET
    @Path("{id}/entity/{entityId}")
    fun getEntity(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long): EntityView {
        log.debug("getting entity by typeId={} and entityId={}", id, entityId)
        safely {
            return storeService.getEntity(id, entityId)
        }
    }


    @PUT
    @Path("{id}/entity/{entityId}")
    fun updateEntity(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long,
            vo: ChangeSummary): EntityView {
        if (log.isDebugEnabled) {
            log.debug("updating entity for type {} and id {}. ChangeSummary: {}", id, entityId, toString(vo))
        }
        safely {
            return storeService.updateEntity(id, entityId, vo)
        }

    }

    @POST
    @Path("{id}/entity")
    fun newEntity(
            @PathParam("id") id: Int,
            vo: ChangeSummary): EntityView {
        if (log.isDebugEnabled) {
            log.debug("creating entity for type {} and ChangeSummary: {}", id, toString(vo))
        }
        safely {
            return storeService.newEntity(id, vo)
        }
    }

    @DELETE
    @Path("{id}/entity/{entityId}")
    fun deleteEntity(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long) {
        log.debug("deleting entity for type {} and id {}", id, entityId)
        safely {
            storeService.deleteEntity(id, entityId)
        }
    }

    @GET
    @Path("{id}/entity/{entityId}/blob/{blobName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM + ";charset=utf-8")
    fun getBlob(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long,
            @PathParam("blobName") blobName: String): StreamingOutput {
        log.debug("getting entity blob data for type {} and id {} and blob '{}'", id, entityId, blobName)
        return StreamingOutput {
            safely {
                storeService.getBlob(id, entityId, blobName, it)
            }
        }
    }

    private fun toString(vo: ChangeSummary): String {
        try {
            return configurator.mapper.writeValueAsString(vo)
        } catch (e: JsonProcessingException) {
            return "Error converting vo to string. Check the server state this error should never happened"
        }
    }
}