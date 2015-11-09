package com.lehvolk.xodus.repo;

import lombok.Getter;

/**
 * @author Alexey Volkov
 * @since 09.11.2015
 */

@Getter
public class InvalidFieldException extends RuntimeException {

    private static final long serialVersionUID = 6854568789697231238L;

    private final String fieldName;
    private final String fieldValue;

    public InvalidFieldException(Throwable cause, String fieldName, String fieldValue) {
        super(cause);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
