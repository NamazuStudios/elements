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
 * Loads an instance of {@link Resource} into the {@link ResourceService}, executes the {@link Request},
 * collects the {@link Response} and promptly removes and destroys the {@link Resource} once the
 * requested method provides its final result.
 */
public class RequestScopedHttpSessionDispatcher implements SessionRequestDispatcher<HttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RequestScopedHttpSessionDispatcher.class);

    private List<Filter> filterList;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private Scheduler scheduler;

    private ExceptionMapper.Resolver exceptionMapperResolver;

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
        final Resource resource = getResourceLoader().load(httpModule.getModule());

        getResourceService().addResource(path, resource);
        schedule(httpOperation, path, session, request, responseConsumer);

    }

    private void schedule(final HttpOperation httpOperation,
                          final Path path,
                          final Session session,
                          final Request request,
                          final Consumer<Response> responseConsumer) {
        getScheduler().performV(path, resource -> safelyDispatch(resource, path, httpOperation, session, request, responseConsumer));
    }

    private void safelyDispatch(final Resource resource,
                                final Path path,
                                final HttpOperation httpOperation,
                                final Session session,
                                final Request request,
                                final Consumer<Response> responseConsumer) {

        final Consumer<Response> closingResponseConsumer = response -> {
            try {
                responseConsumer.accept(response);
            } finally {
                getResourceService().removeAndCloseResource(path);
            }
        };

        getExceptionMapperResolver().protect(request, responseConsumer, () ->
            resource.getModuleDispatcher(httpOperation.getName())
                    .params(request.getPayload(), request, session)
                    .forResultType(Response.class)
                    .withConsumer(closingResponseConsumer)).perform();

    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    @Inject
    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ExceptionMapper.Resolver getExceptionMapperResolver() {
        return exceptionMapperResolver;
    }

    @Inject
    public void setExceptionMapperResolver(ExceptionMapper.Resolver exceptionMapperResolver) {
        this.exceptionMapperResolver = exceptionMapperResolver;
    }

}
