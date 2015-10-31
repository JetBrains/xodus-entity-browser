package com.lehvolk.xodus.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
public class EntityPropertyVO extends BaseVO {

	private String name;
	private String type;
	private String value;
}
