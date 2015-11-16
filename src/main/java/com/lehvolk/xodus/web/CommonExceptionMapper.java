package com.lehvolk.xodus.web;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.lehvolk.xodus.web.exceptions.XodusRestException;
import com.lehvolk.xodus.web.vo.ServerErrorVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexey Volkov
 * @since 16.11.2015
 */
@Slf4j
@Provider
public class CommonExceptionMapper implements ExceptionMapper<XodusRestException> {

    @Override
    public Response toResponse(XodusRestException e) {
        String message = "Internal server error. Getting "
                + e.getCause().getClass() + ": " + e.getCause().getMessage() + ". Check server log for more details.";
        ServerErrorVO vo = new ServerErrorVO();
        vo.setMsg(message);
        return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(vo).build();
    }

}