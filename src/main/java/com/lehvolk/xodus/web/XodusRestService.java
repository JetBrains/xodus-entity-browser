package com.lehvolk.xodus.web;

import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lehvolk.xodus.web.exceptions.EntityNotFoundException;
import com.lehvolk.xodus.web.exceptions.InvalidFieldException;
import com.lehvolk.xodus.web.exceptions.XodusRestException;
import com.lehvolk.xodus.web.services.PersistentStoreService;
import com.lehvolk.xodus.web.vo.ChangeSummaryVO;
import com.lehvolk.xodus.web.vo.EntityTypeVO;
import com.lehvolk.xodus.web.vo.EntityVO;
import com.lehvolk.xodus.web.vo.SearchPagerVO;
import jetbrains.exodus.entitystore.EntityStoreException;
import lombok.extern.slf4j.Slf4j;
import static java.lang.Math.min;

/**
 * REST service for PersistentStore
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Path("/")
public class XodusRestService {

    @Inject
    private PersistentStoreService persistentStoreService;

    @Inject
    private JacksonConfigurator configurator;

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityTypeVO[] getAllTypes() {
        log.debug("getting all entity types");
        try {
            return persistentStoreService.getTypes();
        } catch (RuntimeException e) {
            log.error("error getting all types", e);
            throw new NotFoundException(e);
        }
    }

    @GET
    @Path("/type/{id}/entities")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchPagerVO searchEntities(
            @PathParam("id") int id,
            @QueryParam("q") String term,
            @QueryParam("offset") int offset,
            @QueryParam("pageSize") int pageSize) {
        log.debug("searching entities by typeId: {}, term [{}] with offset = {} and pageSize = {}",
                id, term, offset, pageSize);
        if (offset < 0 || pageSize < 0) {
            throw new BadRequestException();
        }
        try {
            return persistentStoreService.searchType(id, term, offset, (pageSize == 0) ? 50 : min(pageSize, 1000));
        } catch (RuntimeException e) {
            log.error("error searching entities", e);
            throw new NotFoundException(e);
        }
    }

    @GET
    @Path("/type/{id}/entity/{entityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityVO getEntity(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId) {
        log.debug("getting entity by typeId={} and entityId={}", id, entityId);
        try {
            return persistentStoreService.getEntity(id, entityId);
        } catch (EntityNotFoundException e) {
            log.error("getting entity failed", e);
            throw e;
        } catch (EntityStoreException e) {
            log.error("error getting entity", e);
            throw new NotFoundException(e);
        }
    }


    @PUT
    @Path("/type/{id}/entity/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EntityVO updateEntity(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId,
            ChangeSummaryVO vo) {
        if (log.isDebugEnabled()) {
            log.debug("updating entity for type {} and id {}. ChangeSummary: {}", id, entityId, toString(vo));
        }
        try {
            return persistentStoreService.updateEntity(id, entityId, vo);
        } catch (InvalidFieldException | EntityNotFoundException e) {
            log.error("error updating entity", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("error updating entity", e);
            throw new XodusRestException(e);
        }
    }

    @POST
    @Path("/type/{id}/entity")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EntityVO newEntity(
            @PathParam("id") int id,
            ChangeSummaryVO vo) {
        if (log.isDebugEnabled()) {
            log.debug("creating entity for type {} and ChangeSummary: {}", id, toString(vo));
        }
        try {
            return persistentStoreService.newEntity(id, vo);
        } catch (InvalidFieldException | EntityNotFoundException e) {
            log.error("error creating entity", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("error creating entity", e);
            throw new XodusRestException(e);
        }
    }

    @DELETE
    @Path("/type/{id}/entity/{entityId}")
    public void deleteEntity(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId) {
        log.debug("deleting entity for type {} and id {}", id, entityId);
        try {
            persistentStoreService.deleteEntity(id, entityId);
        } catch (EntityNotFoundException e) {
            log.error("error deleting entity", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("error deleting entity", e);
            throw new XodusRestException(e);
        }
    }

    @GET
    @Path("/type/{id}/entity/{entityId}/blob/{blobName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getBlob(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId,
            @PathParam("blobName") String blobName) {
        log.debug("getting entity blob data for type {} and id {} and blob '{}'", id, entityId, blobName);
        return outputStream -> {
            try {
                persistentStoreService.getBlob(id, entityId, blobName, outputStream);
            } catch (EntityNotFoundException e) {
                log.error("entity not found", e);
                throw e;
            } catch (IOException e) {
                log.error("error getting blob:", e);
                throw e;
            }
        };
    }


    private String toString(ChangeSummaryVO vo) {
        try {
            return configurator.getMapper().writeValueAsString(vo);
        } catch (JsonProcessingException e) {
            return "Error converting vo to string. Check the server state this error should never happened";
        }
    }
}
