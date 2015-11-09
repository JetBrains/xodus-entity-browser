package com.lehvolk.xodus.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.lehvolk.xodus.vo.EntityVO.EntityPropertyVO;
import com.lehvolk.xodus.vo.EntityVO.LinkPropertyVO;
import lombok.Getter;
import lombok.Setter;

/**
 * change summary for entity
 * @author Alexey Volkov
 * @since 08.11.2015
 */
@Getter
@Setter
public class ChangeSummaryVO implements Serializable {

    private static final long serialVersionUID = 8633118285398187560L;

    @Getter
    @Setter
    public static class ChangeSummarySection<T extends Serializable> implements Serializable {
        private static final long serialVersionUID = -3027713892232030098L;

        private List<T> added = new ArrayList<>();
        private List<T> deleted = new ArrayList<>();
        private List<T> modified = new ArrayList<>();

    }

    @Getter
    @Setter
    public static class PropertiesSection extends ChangeSummarySection<EntityPropertyVO> {

        private static final long serialVersionUID = -3027713892232030098L;
    }

    @Getter
    @Setter
    public static class LinksSection extends ChangeSummarySection<LinkPropertyVO> {

        private static final long serialVersionUID = -3027713892232030098L;
    }

    private PropertiesSection properties;
    private LinksSection links;

}
