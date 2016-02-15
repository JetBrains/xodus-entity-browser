package com.lehvolk.xodus.web

import com.fasterxml.jackson.core.JsonProcessingException
import jetbrains.exodus.entitystore.EntityStoreException
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.StreamingOutput

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class XodusRestService {

    private val log = LoggerFactory.getLogger(javaClass)

    val persistentStoreService = DI.persistenceService

    private val configurator = JacksonConfigurator()

    @GET
    @Path("/db")
    fun getDBSummary(): DBSummary {
        log.debug("getting database summary")
        try {
            val result = DBSummary()
            val requisites = XodusStore.current();
            result.location = requisites?.location
            result.key = requisites?.key
            result.types = persistentStoreService.allTypes().sortedBy { it.name }
            result.recent = Databases.allRecent()
            return result
        } catch (e: RuntimeException) {
            log.error("error getting all types", e)
            throw NotFoundException(e)
        }
    }

    @GET
    @Path("/type/{id}/entities")
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
        try {
            return persistentStoreService.searchType(id, term, offset, if (pageSize == 0) 50 else Math.min(pageSize, 1000))
        } catch (e: RuntimeException) {
            log.error("error searching entities", e)
            throw NotFoundException(e)
        }

    }

    @GET
    @Path("/type/{id}/entity/{entityId}")
    fun getEntity(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long): EntityView {
        log.debug("getting entity by typeId={} and entityId={}", id, entityId)
        try {
            return persistentStoreService.getEntity(id, entityId)
        } catch (e: EntityNotFoundException) {
            log.error("getting entity failed", e)
            throw e
        } catch (e: EntityStoreException) {
            log.error("error getting entity", e)
            throw NotFoundException(e)
        }
    }


    @PUT
    @Path("/type/{id}/entity/{entityId}")
    fun updateEntity(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long,
            vo: ChangeSummary): EntityView {
        if (log.isDebugEnabled) {
            log.debug("updating entity for type {} and id {}. ChangeSummary: {}", id, entityId, toString(vo))
        }
        try {
            return persistentStoreService.updateEntity(id, entityId, vo)
        } catch (e: InvalidFieldException) {
            log.error("error updating entity", e)
            throw e
        } catch (e: EntityNotFoundException) {
            log.error("error updating entity", e)
            throw e
        } catch (e: RuntimeException) {
            log.error("error updating entity", e)
            throw XodusRestException(e)
        }
    }

    @POST
    @Path("/type/{id}/entity")
    fun newEntity(
            @PathParam("id") id: Int,
            vo: ChangeSummary): EntityView {
        if (log.isDebugEnabled) {
            log.debug("creating entity for type {} and ChangeSummary: {}", id, toString(vo))
        }
        try {
            return persistentStoreService.newEntity(id, vo)
        } catch (e: InvalidFieldException) {
            log.error("error creating entity", e)
            throw e
        } catch (e: EntityNotFoundException) {
            log.error("error creating entity", e)
            throw e
        } catch (e: RuntimeException) {
            log.error("error creating entity", e)
            throw XodusRestException(e)
        }
    }

    @DELETE
    @Path("/type/{id}/entity/{entityId}")
    fun deleteEntity(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long) {
        log.debug("deleting entity for type {} and id {}", id, entityId)
        try {
            persistentStoreService.deleteEntity(id, entityId)
        } catch (e: EntityNotFoundException) {
            log.error("error deleting entity", e)
            throw e
        } catch (e: RuntimeException) {
            log.error("error deleting entity", e)
            throw XodusRestException(e)
        }
    }

    @GET
    @Path("/type/{id}/entity/{entityId}/blob/{blobName}")
    fun getBlob(
            @PathParam("id") id: Int,
            @PathParam("entityId") entityId: Long,
            @PathParam("blobName") blobName: String): StreamingOutput {
        log.debug("getting entity blob data for type {} and id {} and blob '{}'", id, entityId, blobName)
        return StreamingOutput {
            try {
                persistentStoreService.getBlob(id, entityId, blobName, it)
            } catch (e: EntityNotFoundException) {
                log.error("entity not found", e)
                throw e
            } catch (e: IOException) {
                log.error("error getting blob:", e)
                throw e
            }
        }
    }

    @POST
    @Path("/db")
    fun updateDB(db: DB) {
        XodusStore.use(XodusStoreRequisites(db.location!!, db.key!!))
        persistentStoreService.destroy()
        persistentStoreService.update()
        try {
            Databases.add(db)
        } catch(e: Exception) {
            log.error("error adding dababase to recent")
            throw XodusRestException(e)
        }
    }

    @DELETE
    @Path("/db")
    fun deleteDB(db: DB) {
        try {
            Databases.delete(db)
        } catch(e: Exception) {
            log.error("error adding dabase to recent")
            throw XodusRestException(e)
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