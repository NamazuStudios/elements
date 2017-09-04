package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;

/**
 * Loads an instance of {@link Resource} into the {@link ResourceService}, executes the {@link Request},
 * collects the {@link Response} and prompty removes and destroys the {@link Resource}.
 */
public class RequestScopedHttpSessionDispatcher implements SessionRequestDispatcher<HttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RequestScopedHttpSessionDispatcher.class);

    private List<Filter> filterList;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    @Override
    public void dispatch(final Session session,
                         final HttpRequest request,
                         final Consumer<Response> responseConsumer) {
        final Resource resource = openForRequest(request);
        final Filter.Chain chain;
        chain = Filter.Chain.build(getFilterList(), (s, r, rc) -> dispatch(resource, s, r, rc));
        chain.next(session, request, responseConsumer);
    }

    private Resource openForRequest(HttpRequest request) {
        return null;
    }

    private void dispatch(Resource resource, Session session, Request r, Consumer<Response> responseConsumer) {
        resource.getDispatcher(r.getHeader().getMethod())
                .dispatch(r, session)
                .forResultType(Response.class)
                .withConsumer(responseConsumer.andThen(response -> resource.close()));
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

}
