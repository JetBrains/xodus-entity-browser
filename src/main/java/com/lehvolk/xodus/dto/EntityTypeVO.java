package com.lehvolk.xodus.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
public class EntityTypeVO extends BaseVO {

	private String name;
	private List<EntityPropertyVO> properties = new ArrayList<>();

}
