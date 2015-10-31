package com.lehvolk.xodus.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
public class EntityVO extends BaseVO {

	private List<EntityPropertyVO> values;
	private Map<String, Object> links;
	private Map<String, Object> blobs;

}
