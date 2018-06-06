package com.namazustudios.socialengine.rt;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static java.util.UUID.randomUUID;

/**
 * Keeps {@link Resource} instances cached in memory such that they may be recycled for one-time method invocations.
 */
public class SimpleSingleUseHandlerService implements SingleUseHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSingleUseHandlerService.class);

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceLockService resourceLockService;

    private Map<Key, Queue<Resource>> moduleCache;

    @Override
    public void start() {
        moduleCache = new ConcurrentHashMap<>();
    }

    private void releaseAndDestroy(final Resource resource) {

        final ResourceId resourceId = resource.getId();

        try {
            getResourceService().release(resource);
        } catch (Exception ex) {
            logger.error("Caught error releasing resource.", ex);
        }

        try {
            getResourceService().destroy(resourceId);
        } catch (Exception ex) {
            logger.error("Caught error releasing resource.", ex);
        }

    }

    @Override
    public void stop() {
        moduleCache.forEach((k, q) -> q.forEach(this::releaseAndDestroy));
        moduleCache.clear();
        moduleCache = null;
    }

    @Override
    public <T> T perform(final Attributes attributes, final String module, final Function<Resource, T> operation) {
        try (final Operation o = new Operation(attributes, module)) {
            return o.perform(operation);
        }
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public ResourceLockService getResourceLockService() {
        return resourceLockService;
    }

    @Inject
    public void setResourceLockService(ResourceLockService resourceLockService) {
        this.resourceLockService = resourceLockService;
    }

    private class Operation implements AutoCloseable {

        private final Key key;

        private final Resource resource;

        private final ResourceId resourceId;

        private final Queue<Resource> queue;

        public Operation(final Attributes attributes, final String module) {
            key = new Key(attributes, module);
            queue = getQueue();
            resource = getOrLoad();
            resourceId = resource.getId();
        }

        private Queue<Resource> getQueue() {
            return moduleCache.computeIfAbsent(key,  k -> new ConcurrentLinkedQueue<>());
        }

        private Resource getOrLoad() {

            Resource resource = queue.poll();
            if (resource != null) return resource;

            resource = getResourceLoader().load(key.getModule(), key.getAttributes());
            final Path path = Path.fromComponents("tmp", "handler", randomUUID().toString());
            return getResourceService().addAndAcquireResource(path, resource);

        }

        public <T> T perform(final Function<Resource, T> operation) {
            try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {
                return operation.apply(resource);
            }
        }

        @Override
        public void close() {
            queue.add(resource);
        }

    }

    private static class Key {

        private final String module;

        private final Attributes attributes;

        public Key(final Attributes attributes, final String module) {
            this.module = module;
            this.attributes = attributes;
        }

        public String getModule() {
            return module;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return Objects.equals(module, key.module) &&
                   Objects.equals(attributes, key.attributes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(module, attributes);
        }

    }

}
