package com.lehvolk.xodus.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
public class EntityPresentationVO extends BaseVO {

	private static final long serialVersionUID = 8816524086938064511L;
	private String label;
	private String details;
}
