package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.Path.fromComponents;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;

public class SimpleHandlerContext implements HandlerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandlerContext.class);

    private ResourceContext resourceContext;

    @Override
    public Future<Object> invokeRemoteHandlerAsync(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final Attributes attributes, final String module,
            final String method, final Object... args) {

        final Path path = fromComponents("handler", randomUUID().toString());
        final ResourceId resourceId = getResourceContext().createAttributes(module, path, attributes);

        try {
            return getResourceContext().invokeAsync(
                success(resourceId, success, module, method, args),
                failure(resourceId, failure, module, method, args),
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

    private <T> Consumer<T> success(
            final ResourceId resourceId,
            final Consumer<T> consumer,
            final String module,
            final String method,
            final Object[] args) {
        return o -> {
            try {
                logger.info("Successful Result for {}.{}({})", module, method,
                    Stream.of(args)
                          .map(a -> a == null ? null : a.toString())
                          .collect(joining(",")));
                consumer.accept(o);
            } finally {
                destroyAndLog(resourceId);
            }
        };
    }

    private <T extends Throwable> Consumer<T> failure(
            final ResourceId resourceId,
            final Consumer<Throwable> consumer,
            final String module,
            final String method,
            final Object[] args) {
        return th -> {
            try {
                logger.info("Unsuccessful Result for {}.{}({})", module, method,
                    Stream.of(args)
                          .map(a -> a == null ? null : a.toString())
                          .collect(joining(",")), th);
                consumer.accept(th);
            } catch (Exception ex) {
                logger.error("Exception", ex);
            } finally {
                destroyAndLog(resourceId);
            }
        };
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
