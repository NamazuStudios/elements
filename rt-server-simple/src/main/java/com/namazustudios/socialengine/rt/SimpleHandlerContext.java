package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.Path.fromComponents;
import static java.util.UUID.randomUUID;

public class SimpleHandlerContext implements HandlerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandlerContext.class);

    private ResourceContext resourceContext;

    @Override
    public Future<Object> invokeRemoteHandlerAsync(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final Attributes attributes, final String module,
            final String method, final Object... args) {

        final Path path = fromComponents("handler", randomUUID().toString());
        final ResourceId resourceId = getResourceContext().create(module, path);

        try {
            return getResourceContext().invokeAsync(
                o -> {
                    destroyAndLog(resourceId);
                    success.accept(o);
                },
                th -> {
                    destroyAndLog(resourceId);
                    failure.accept(th);
                },
                resourceId, method, args);
        } catch (RuntimeException ex) {
            destroyAndLog(resourceId);
            failure.accept(ex);
            throw ex;
        } catch (Exception ex) {
            destroyAndLog(resourceId);
            failure.accept(ex);
            throw new InternalException(ex);
        }

    }

    private void destroyAndLog(final ResourceId resourceId) {
        getResourceContext().destroyAsync(
            v -> logger.info("Destroyed {}.", resourceId),
            th -> logger.error("Failed to destroy {}", resourceId),
            resourceId);
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Inject
    public void setResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

}
