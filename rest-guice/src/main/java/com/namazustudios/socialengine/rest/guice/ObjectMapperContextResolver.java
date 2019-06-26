package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;

// No @Provider anntoated to avoid accidentally picking it up in a recursive scan
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private ObjectMapper mapper;

    @Override
    public ObjectMapper getContext(Class<?> type) {

        if (!ObjectMapper.class.equals(type)) {
            final String msg = "Expected " + type.getName();
            throw new IllegalArgumentException(msg);
        }

        return getMapper();

    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

}
