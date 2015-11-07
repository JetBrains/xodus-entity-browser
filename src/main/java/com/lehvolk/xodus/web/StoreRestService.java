package com.lehvolk.xodus.web;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.lehvolk.xodus.dto.EntityTypeVO;
import com.lehvolk.xodus.dto.EntityVO;
import com.lehvolk.xodus.dto.SearchPagerVO;
import com.lehvolk.xodus.repo.PersistentStoreService;

/**
 * Api rest service for repo
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Path("/")
public class StoreRestService {

    @Inject
    private PersistentStoreService persistentStoreService;

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityTypeVO[] getAllTypes() {
        return persistentStoreService.getTypes();
    }

    @GET
    @Path("/type/{id}/entities")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchPagerVO searchEntities(
            @PathParam("id") int id,
            @QueryParam("q") String term,
            @QueryParam("offset") int offset,
            @QueryParam("pageSize") int pageSize) {
        if (offset < 0 || pageSize < 0) {
            throw new BadRequestException();
        }
        return persistentStoreService.searchType(id, term, offset, (pageSize == 0) ? 50 : pageSize);
    }

    @GET
    @Path("/type/{id}/entity/{entityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityVO getEntity(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId) {
        return persistentStoreService.getEntity(id, entityId);
    }
}
