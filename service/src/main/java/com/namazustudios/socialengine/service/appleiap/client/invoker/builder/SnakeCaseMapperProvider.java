package com.namazustudios.socialengine.service.appleiap.client.invoker.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class SnakeCaseMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper mapper;

    public SnakeCaseMapperProvider() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    @Override
    public ObjectMapper getContext(Class<?> cls) {
        return mapper;
    }

}
