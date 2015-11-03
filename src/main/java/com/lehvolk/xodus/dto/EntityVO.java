package com.lehvolk.xodus.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
public class EntityVO extends BaseVO {

	private static final long serialVersionUID = -6471237580582518615L;
	private List<EntityPropertyVO> values;
	private Map<String, Object> links;
	private Map<String, Object> blobs;

}
