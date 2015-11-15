package com.lehvolk.xodus.web.vo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 12.11.2015
 */
@Getter
@Setter
public class ServerErrorVO implements Serializable {
    private static final long serialVersionUID = -3955842171582403977L;
    private String msg;
}
