package dev.getelements.elements.common.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.model.annotation.ClientSerializationStrategy;

import jakarta.inject.Inject;
import jakarta.ws.rs.ext.ContextResolver;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static dev.getelements.elements.sdk.model.annotation.ClientSerializationStrategy.DEFAULT;

public class ClientObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private Map<String, ObjectMapper> objectMappers;

    // WeakHashMap so Class keys from hot-loaded/disposed Element classloaders don't get pinned in memory.
    private final Map<Class<?>, ObjectMapper> cache = new WeakHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public ObjectMapper getContext(final Class<?> type) {

        lock.readLock().lock();

        try {
            final ObjectMapper existing = cache.get(type);
            if (existing != null) {
                return existing;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();

        try {
            return cache.computeIfAbsent(type, this::load);
        } finally {
            lock.writeLock().unlock();
        }

    }

    private ObjectMapper load(final Class<?> type) {

        final ClientSerializationStrategy strategy = type.getAnnotation(ClientSerializationStrategy.class);
        final String name = strategy == null ? DEFAULT : strategy.value();
        final ObjectMapper objectMapper = getObjectMappers().get(name);

        if (objectMapper == null) {
            throw new IllegalStateException("No ObjectMapper configured for serialization strategy: " + strategy);
        }

        return objectMapper;

    }

    public Map<String, ObjectMapper> getObjectMappers() {
        return objectMappers;
    }

    @Inject
    public void setObjectMappers(Map<String, ObjectMapper> objectMappers) {
        this.objectMappers = objectMappers;
    }

}
