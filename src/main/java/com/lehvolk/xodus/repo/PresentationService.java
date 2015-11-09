package com.lehvolk.xodus.repo;

import java.util.function.Function;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.lehvolk.xodus.vo.EntityVO;
import com.lehvolk.xodus.vo.EntityVO.EntityPropertyVO;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Singleton
public class PresentationService {

    private static final Pattern ID_PATTERN = Pattern.compile("\\{\\{id\\}\\}");

    @Inject
    private ConfigurationService service;

    public Function<EntityVO, EntityVO> presentation(final long typeId, final String type) {
        return entity -> {
            entity.setLabel(type + "[" + format(service.getLabelFormat(typeId), entity) + "]");
            return entity;
        };
    }

    private String format(String format, EntityVO entityVO) {
        String formatted = ID_PATTERN.matcher(format).replaceAll(String.valueOf(entityVO.getId()));
        for (EntityPropertyVO property : entityVO.getProperties()) {
            formatted = formatted.replaceAll("\\{\\{" + property + "\\}\\}", String.valueOf(property.getValue()));
            if (!formatted.contains("\\{\\{\\.*\\}\\}")) {
                return formatted;
            }
        }
        return formatted;
    }

}
