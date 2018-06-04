package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.UUID.randomUUID;

public class SimpleRetainedHandlerService implements RetainedHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRetainedHandlerService.class);

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    @Override
    public TaskId perform(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final Attributes attributes, final String module, final String method, final Object... args) {

        final AtomicReference<Path> pathAtomicReference = new AtomicReference<>();

        final Runnable unlink = () -> {

            if (pathAtomicReference.get() == null) return;

            try {
                getResourceService().unlinkPath(pathAtomicReference.get());
            } catch (Exception ex) {
                failure.accept(ex);
                logger.error("Caught exception unlinking path {}", pathAtomicReference.get(), ex);
            }

        };

        final Consumer<Object> _success = o -> {
            try {
                success.accept(o);
            } finally {
                unlink.run();
            }
        };

        final Consumer<Throwable> _failure = th -> {
            try {
                failure.accept(th);
            } finally {
                unlink.run();
            }
        };

        try (final Operation o = new Operation(attributes, module)) {
            return o.perform((r, p)->  {
                pathAtomicReference.set(p);
                return r.getMethodDispatcher(method)
                        .params(args)
                        .dispatch(_success, _failure);
            });
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

    private class Operation implements AutoCloseable {

        private final Path path;

        private final Resource resource;

        public Operation(final Attributes attributes, final String module) {

            path = Path.fromComponents("tmp", "handler", randomUUID().toString());

            final Resource r = getResourceLoader().load(module, attributes);
            resource = getResourceService().addAndAcquireResource(path, r);

        }

        public <T> T perform(final BiFunction<Resource, Path, T> resourceTFunction) {
            return resourceTFunction.apply(resource, path);
        }

        @Override
        public void close() {
            getResourceService().release(resource);
        }

    }

}
