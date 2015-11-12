package com.lehvolk.xodus.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Alexey Volkov
 * @since 06.11.2015
 */
@Getter
@Setter
@AllArgsConstructor
public class SearchPagerVO implements Serializable {

    private static final long serialVersionUID = -4270545504789259512L;

    private LightEntityVO[] items;
    private long totalCount;
}
