package com.lehvolk.xodus.repo;

import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Singleton
public class ConfigurationService {

    public String getLabelFormat(long entityTypeId) {
        return "{{id}}";
    }

    public String getDetailsFormat(long entityTypeId) {
        return "some information";
    }
}
