package com.lehvolk.xodus.web;

import com.lehvolk.xodus.dto.EntityPresentationVO;
import com.lehvolk.xodus.dto.EntityTypeVO;
import com.lehvolk.xodus.repo.RepoService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Path("/")
public class RepoRestService {

	@Inject
	private RepoService repoService;

	@GET
	@Path("/types")
	@Produces(MediaType.APPLICATION_JSON)
	public List<EntityTypeVO> getTypes() {
		return repoService.getTypes();
	}

	@GET
	@Path("/type/{id}/entities")
	@Produces(MediaType.APPLICATION_JSON)
	public List<EntityPresentationVO> searchEntities(@PathParam("id") long id, @QueryParam("q") String term) {
		return repoService.searchType(id, term);
	}


}
