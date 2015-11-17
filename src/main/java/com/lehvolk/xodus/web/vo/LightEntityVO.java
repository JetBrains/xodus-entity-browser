package com.lehvolk.xodus.web.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Light VO for entity presentation
 * @author Alexey Volkov
 * @since 12.11.2015
 */
@Getter
@Setter
public class LightEntityVO extends BaseVO {

    private static final long serialVersionUID = 48319242411151430L;

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
        private String value;
        private EntityPropertyTypeVO type;
    }

    @Getter
    @Setter
    public static class EntityPropertyTypeVO implements Serializable {

        private static final long serialVersionUID = -4446983251527745550L;
        private boolean readonly;
        private String clazz;
        private String displayName;
    }

    private String label;
    private String type;
    private String typeId;
    private List<EntityPropertyVO> properties;

}
