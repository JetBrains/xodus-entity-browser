package com.lehvolk.xodus.web.vo;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
@XmlRootElement
public class BaseVO implements Serializable {

	private static final long serialVersionUID = 2406560538582931684L;

	private String id;

}
