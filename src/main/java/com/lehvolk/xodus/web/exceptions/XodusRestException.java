package com.lehvolk.xodus.web.exceptions;

import javax.ws.rs.InternalServerErrorException;

/**
 * Exception Xodus database
 * @author Alexey Volkov
 * @since 16.11.2015
 */
public class XodusRestException extends InternalServerErrorException {

    private static final long serialVersionUID = 317880648307744850L;

    public XodusRestException(Throwable cause) {
        super(cause);
    }
}
