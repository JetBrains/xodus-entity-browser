package com.lehvolk.xodus.web.services;

import java.util.function.Function;
import javax.inject.Singleton;

import com.lehvolk.xodus.web.vo.LightEntityVO;
import com.lehvolk.xodus.web.vo.LightEntityVO.EntityPropertyVO;

/**
 * Presentation service used for creating label for entities.
 * <p>
 * Type, entity id and entity properties can be used for label generation.
 *
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Singleton
public class PresentationService {

    public <T extends LightEntityVO> Function<T, T> presentationOf(final int typeId, final String type) {
        return entity -> {
            String labelFormat = getLabelFormat(typeId);
            labelFormat = labelFormat.replaceAll(wrap("type"), type);
            entity.setLabel(format(labelFormat, entity));
            return entity;
        };
    }

    public String getLabelFormat(int typeId) {
        return "{{type}}[{{id}}]";
    }

    private String format(String format, LightEntityVO entityVO) {
        String formatted = format.replaceAll(wrap("id"), String.valueOf(entityVO.getId()));
        for (EntityPropertyVO property : entityVO.getProperties()) {
            formatted = formatted.replaceAll(wrap("entity." + property), String.valueOf(property.getValue()));
            if (!formatted.contains("\\{\\{\\.*\\}\\}")) {
                return formatted;
            }
        }
        return formatted;
    }

    private String wrap(String key) {
        return "\\{\\{" + key + "\\}\\}";
    }

}
