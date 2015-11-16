package com.lehvolk.xodus.web;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

/**
 * provider for object mapper
 * @author Alexey Volkov
 * @since 14.11.2015
 */
@Provider
public class JacksonConfigurator implements ContextResolver<ObjectMapper> {

    @Getter
    private final ObjectMapper mapper;

    public JacksonConfigurator() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

}