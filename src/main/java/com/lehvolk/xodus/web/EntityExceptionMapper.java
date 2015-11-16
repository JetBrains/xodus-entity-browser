package com.lehvolk.xodus.web;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.lehvolk.xodus.web.exceptions.EntityNotFoundException;
import com.lehvolk.xodus.web.vo.ServerErrorVO;

/**
 * @author Alexey Volkov
 * @since 16.11.2015
 */
@Provider
public class EntityExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

    @Override
    public Response toResponse(EntityNotFoundException e) {
        ServerErrorVO vo = new ServerErrorVO();
        String message = "Error getting entity by type '" + e.getTypeId() + "' and id='" + e
                .getEntityId() + "'";
        vo.setMsg(message);
        return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(vo).build();
    }

}