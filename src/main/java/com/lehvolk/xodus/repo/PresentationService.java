package com.lehvolk.xodus.repo;

import com.lehvolk.xodus.dto.EntityPresentationVO;
import com.lehvolk.xodus.dto.EntityPropertyVO;
import com.lehvolk.xodus.dto.EntityTypeVO;
import com.lehvolk.xodus.dto.EntityVO;

import javax.inject.Singleton;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Singleton
public class PresentationService {

	@javax.inject.Inject
	private ConfigurationService service;

	public EntityPresentationVO presentation(EntityTypeVO typeVo, EntityVO vo){
		EntityPresentationVO presentationVO = new EntityPresentationVO();
		presentationVO.setId(vo.getId());
		presentationVO.setLabel(typeVo.getName() +": " + format(service.getLabelFormat(typeVo.getId()), vo));
		presentationVO.setDetails(format(service.getDetailsFormat(typeVo.getId()), vo));
		return presentationVO;
	}

	private String format(String format, EntityVO vo){
		String f = format.replaceAll("\\{\\{id\\}\\}", String.valueOf(vo.getId()));
		for (EntityPropertyVO propertyVO : vo.getValues()) {
			f = f.replaceAll("\\{\\{"+propertyVO.getName()+"\\}\\}", propertyVO.getValue());
		}
		return f;
	}

}
