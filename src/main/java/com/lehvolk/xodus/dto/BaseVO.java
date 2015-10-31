package com.lehvolk.xodus.dto;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Getter
@Setter
@XmlRootElement
public class BaseVO implements Serializable, Cloneable {

	private long id;

	//todo remove this trash
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
