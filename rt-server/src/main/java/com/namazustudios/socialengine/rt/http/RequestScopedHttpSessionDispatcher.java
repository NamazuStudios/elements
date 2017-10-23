package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
import com.namazustudios.socialengine.rt.manifest.http.HttpModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;

import static java.util.UUID.randomUUID;

/**
 * Loads an instance of {@link Resource} into the {@link ResourceService}, executes the {@link Request}, collects the
 * {@link Response} and promptly removes and destroys the {@link Resource} once the requested method provides its final
 * result.
 */
public class RequestScopedHttpSessionDispatcher implements SessionRequestDispatcher<HttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RequestScopedHttpSessionDispatcher.class);

    private List<Filter> filterList;

    private ExceptionMapper.Resolver exceptionMapperResolver;

    private ResourceContext resourceContext;

    private SchedulerContext schedulerContext;

    @Override
    public void dispatch(final Session session,
                         final HttpRequest httpRequest,
                         final Consumer<Response> responseConsumer) {
        final Filter.Chain chain;
        chain = Filter.Chain.build(getFilterList(), (s, r, rr) -> createAndSchedule(httpRequest, s, r, rr));
        chain.next(session, httpRequest, responseConsumer);
    }

    private void createAndSchedule(final HttpRequest httpRequest,
                                   final Session session,
                                   final Request request,
                                   final Consumer<Response> responseConsumer) {

        final HttpModule httpModule = httpRequest.getManifestMetadata().getModule();
        final HttpOperation httpOperation = httpRequest.getManifestMetadata().getPreferredOperation();

        final Path path = Path.fromComponents("http", "request", randomUUID().toString());
        final ResourceId resourceId = getResourceContext().create(httpModule.getModule(), path);

        logger.info("Created resource with id {} to handle request.", resourceId);
        schedule(httpOperation, path, session, request, responseConsumer);

    }

    private void schedule(final HttpOperation httpOperation,
                          final Path path,
                          final Session session,
                          final Request request,
                          final Consumer<Response> responseConsumer) {
        getSchedulerContext().performV(path, resource -> safelyDispatch(resource, httpOperation, session, request, responseConsumer));
    }

    private void safelyDispatch(final Resource resource,
                                final HttpOperation httpOperation,
                                final Session session,
                                final Request request,
                                final Consumer<Response> responseConsumer) {

        final Consumer<Response> closingResponseConsumer = response -> {
            try {
                responseConsumer.accept(response);
            } finally {

                final ResourceId resourceId = resource.getId();

                getResourceContext().destroyAsync(v  -> logger.info("Destroyed {}", resourceId), th -> logger.error("Failed to destroy {}", resourceId, th), resourceId
                );

            }
        };

        final Consumer<Throwable> handler = ex -> getExceptionMapperResolver()
                .getExceptionMapper(ex)
                .map(ex, request, responseConsumer);

        try {
            resource.getMethodDispatcher(httpOperation.getMethod())
                    .params(request.getPayload(), request, session)
                    .forResultType(Response.class)
                    .dispatch(closingResponseConsumer, handler);
        } catch (Throwable th) {
            logRequestFailure(request, th);
            handler.accept(th);
            throw th;
        }

    }

    private void logRequestFailure(final Request request, final Throwable th) {
        try {
            logger.error("Error with request {} {}",
                    request.getHeader().getMethod(),
                    request.getHeader().getParsedPath().toNormalizedPathString(), th);
        } catch (Exception ex) {
            logger.error("Error with request {} {}", ex);
        }
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    @Inject
    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
    }

    public ExceptionMapper.Resolver getExceptionMapperResolver() {
        return exceptionMapperResolver;
    }

    @Inject
    public void setExceptionMapperResolver(ExceptionMapper.Resolver exceptionMapperResolver) {
        this.exceptionMapperResolver = exceptionMapperResolver;
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Inject
    public void setResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    @Inject
    public void setSchedulerContext(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

}
