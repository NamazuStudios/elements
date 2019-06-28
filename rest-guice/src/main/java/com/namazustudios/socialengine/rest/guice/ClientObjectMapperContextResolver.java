package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.namazustudios.socialengine.annotation.ClientSerializationStrategy;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import java.util.Map;

import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.DEFAULT;

public class ClientObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private Map<String, ObjectMapper> objectMappers;

    private final LoadingCache<Class<?>, ObjectMapper> cache = CacheBuilder.newBuilder()
        .build(new CacheLoader<Class<?>, ObjectMapper>() {
            @Override
            public ObjectMapper load(final Class<?> key) {

                final ClientSerializationStrategy strategy = key.getAnnotation(ClientSerializationStrategy.class);
                final String name = strategy == null ? DEFAULT : strategy.value();
                final ObjectMapper objectMapper = getObjectMappers().get(name);

                if (objectMapper == null) {
                    throw new IllegalStateException("No ObjectMapper configured for serialization strategy: " + strategy);
                }

                return objectMapper;

            }
        });

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return cache.getUnchecked(type);
    }

    public Map<String, ObjectMapper> getObjectMappers() {
        return objectMappers;
    }

    @Inject
    public void setObjectMappers(Map<String, ObjectMapper> objectMappers) {
        this.objectMappers = objectMappers;
    }

}
