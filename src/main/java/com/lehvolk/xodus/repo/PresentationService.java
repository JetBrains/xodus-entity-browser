package com.lehvolk.xodus.repo;

import java.util.function.Function;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.lehvolk.xodus.dto.EntityPresentationVO;
import jetbrains.exodus.entitystore.Entity;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Singleton
public class PresentationService {

    private static final Pattern ID_PATTERN = Pattern.compile("\\{\\{id\\}\\}");

    @Inject
    private ConfigurationService service;

    public Function<Entity, EntityPresentationVO> presentation(final long typeId, final String type) {
        return entity -> {
            EntityPresentationVO presentationVO = new EntityPresentationVO();
            presentationVO.setId(entity.getId().getLocalId());
            presentationVO.setLabel(type + ": " + format(service.getLabelFormat(typeId), entity));
            presentationVO.setDetails(format(service.getDetailsFormat(typeId), entity));
            return presentationVO;
        };
    }

    private String format(String format, Entity entity) {
        String formatted = ID_PATTERN.matcher(format).replaceAll(String.valueOf(entity.getId().getLocalId()));
        for (String property : entity.getPropertyNames()) {
            Comparable<?> rawValue = entity.getProperty(property);
            String value = rawValue == null ? "" : rawValue.toString();
            formatted = formatted.replaceAll("\\{\\{" + property + "\\}\\}", value);
        }
        return formatted;
    }

}
