package com.lehvolk.xodus.web.services;

import java.util.function.Function;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.lehvolk.xodus.web.vo.LightEntityVO;
import com.lehvolk.xodus.web.vo.LightEntityVO.EntityPropertyVO;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Singleton
public class PresentationService {

    private static final Pattern ID_PATTERN = Pattern.compile("\\{\\{id\\}\\}");

    @Inject
    private ConfigurationService service;

    public <T extends LightEntityVO> Function<T, T> presentation(final int typeId, final String
            type) {
        return (T entity) -> {
            entity.setLabel(type + "[" + format(service.getLabelFormat(typeId), entity) + "]");
            return entity;
        };
    }

    private String format(String format, LightEntityVO entityVO) {
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
