package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Consumer;

public class RequestScopedHttpSessionDispatcher implements HttpSessionRequestDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(RequestScopedHttpSessionDispatcher.class);

    private Set<Filter> filterSet;

    @Override
    public void dispatch(final Session session,
                         final HttpRequest request,
                         final Consumer<HttpResponse> responseReceiver) {

//        try (final DelegatingCheckedResponseReceiver receiver = new DelegatingCheckedResponseReceiver(request, responseReceiver)) {
//            executeRootFilterChain(session, request, receiver);
//        } catch (Exception ex) {
//            LOG.error("Caught exception processing request {}.", request, ex);
//        }

    }


}
