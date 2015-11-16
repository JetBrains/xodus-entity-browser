package com.lehvolk.xodus.web.exceptions;

import lombok.Getter;

/**
 * @author Alexey Volkov
 * @since 16.11.2015
 */
@Getter
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 7677466476941102003L;
    private final int typeId;
    private final long entityId;

    public EntityNotFoundException(Throwable cause, int typeId, long entityId) {
        super(cause);
        this.typeId = typeId;
        this.entityId = entityId;
    }
}
