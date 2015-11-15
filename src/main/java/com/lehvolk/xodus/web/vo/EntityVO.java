package com.lehvolk.xodus.web.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
public class EntityVO extends LightEntityVO {

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
		private String type;
		private String label;
		private long entityId;
	}

	private static final long serialVersionUID = -6471237580582518615L;

	private List<LinkPropertyVO> links;
	private List<BlobPropertyVO> blobs;

}
