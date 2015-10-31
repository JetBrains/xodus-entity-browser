package com.lehvolk.xodus.repo;

import com.google.common.collect.Lists;
import com.lehvolk.xodus.dto.EntityPresentationVO;
import com.lehvolk.xodus.dto.EntityVO;
import com.lehvolk.xodus.dto.EntityPropertyVO;
import com.lehvolk.xodus.dto.EntityTypeVO;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Singleton
public class RepoService {

	private static List<EntityTypeVO> types;

	@Inject
	private PresentationService presentationService;

	static {
		EntityTypeVO type1 = new EntityTypeVO();
		type1.setId(1);
		type1.setName("Person");
		List<EntityPropertyVO> pProperties = LongStream.range(0, 5).mapToObj(i -> {
			EntityPropertyVO entity = new EntityPropertyVO();
			entity.setId(i);
			entity.setName("p_property" + i);
			return entity;
		}).collect(Collectors.toList());
		type1.getProperties().addAll(pProperties);
		EntityTypeVO type2 = new EntityTypeVO();
		type2.setId(2);
		type2.setName("Organization");
		List<EntityPropertyVO> oProperties = LongStream.range(0, 5).mapToObj(i -> {
			EntityPropertyVO entity = new EntityPropertyVO();
			entity.setId(i);
			entity.setName("o_property" + i);
			return entity;
		}).collect(Collectors.toList());
		type2.getProperties().addAll(oProperties);
		types = Lists.newArrayList(type1, type2);
	}

	@Inject
	private ConfigurationService configuration;

	public List<EntityTypeVO> getTypes() {
		return types;
	}

	public List<EntityPresentationVO> searchType(long typeId, String term) {
		final EntityTypeVO typeVO = getTypes().stream().filter(item -> item.getId() == typeId).findFirst().get();
		final Random r = new Random();
		return LongStream.range(0, 100).mapToObj(i -> {
			EntityVO entity = new EntityVO();
			entity.setId(i);
			entity.setValues(typeVO.getProperties().stream().map(x -> {
				EntityPropertyVO clone = (EntityPropertyVO) x.clone();
				clone.setValue(String.valueOf(r.nextInt(20)));
				return clone;
			}).collect(Collectors.toList()));
			return entity;
		}).map(x -> presentationService.presentation(typeVO, x)).collect(Collectors.toList());
	}

}
