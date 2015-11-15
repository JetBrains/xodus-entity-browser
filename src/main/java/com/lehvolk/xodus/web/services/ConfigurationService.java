package com.lehvolk.xodus.web.services;

import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Singleton
public class ConfigurationService {

    public String getLabelFormat(int entityTypeId) {
        return "{{id}}";
    }

}
