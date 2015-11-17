package com.lehvolk.xodus.web;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.lehvolk.xodus.web.exceptions.InvalidFieldException;
import com.lehvolk.xodus.web.vo.ServerErrorVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexey Volkov
 * @since 12.11.2015
 */
@Slf4j
@Provider
public class ValidationErrorMapper implements ExceptionMapper<InvalidFieldException> {

    @Override
    public Response toResponse(InvalidFieldException e) {
        log.debug("get invalid field {} with value '{}'", e.getFieldName(), e.getFieldValue());
        ServerErrorVO vo = new ServerErrorVO();
        vo.setMsg("invalid value of property '" + e.getFieldName() + "': '" + e.getFieldValue() + "'");
        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(vo).build();
    }

}