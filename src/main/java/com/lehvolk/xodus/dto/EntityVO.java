package com.lehvolk.xodus.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
public class EntityVO extends BaseVO {

	public enum InputType {
		STRING,
		NUMERIC;
	}

	@Getter
	@Setter
	public static class BasePropertyVO implements Serializable {

		private static final long serialVersionUID = -4446983251527745550L;
		private String name;
	}


	@Getter
	@Setter
	public static class EntityPropertyVO extends BasePropertyVO {

		private static final long serialVersionUID = -4446983251527745550L;
		private InputType type;
		private String clazz;
		private Serializable value;
		private String maxLength;
		private Long maxValue;
		private Long minValue;
	}


	@Getter
	@Setter
	public static class BlobPropertyVO extends BasePropertyVO {

		private static final long serialVersionUID = -4446983251527745550L;
		private long blobSize;
	}

	@Getter
	@Setter
	public static class LinkPropertyVO extends BasePropertyVO {

		private static final long serialVersionUID = -4446983251527745550L;
		private int typeId;
		private long entityId;
	}

	private static final long serialVersionUID = -6471237580582518615L;

	private String label;
	private String type;
	private int typeId;
	private List<EntityPropertyVO> properties;
	private List<LinkPropertyVO> links;
	private List<BlobPropertyVO> blobs;

}
