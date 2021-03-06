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
import javax.inject.Named;
import java.io.Serializable;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.Context.REMOTE;

/**
 * Loads an instance of {@link Resource} into the {@link ResourceService}, executes the {@link Request}, collects the
 * {@link Response} and promptly removes and destroys the {@link Resource} once the requested method provides its final
 * result.
 */
public class RequestScopedHttpSessionDispatcher implements SessionRequestDispatcher<HttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RequestScopedHttpSessionDispatcher.class);

    private Filter.Chain.Builder filterChainBuilder;

    private ExceptionMapper.Resolver exceptionMapperResolver;

    private Context context;

    @Override
    public void dispatch(final Session session,
                         final HttpRequest httpRequest,
                         final Consumer<Response> responseConsumer) {
        final Filter.Chain.Builder builder = getFilterChainBuilder();
        final Filter.Chain root = builder.terminate((s, r, rr) -> createAndSchedule(httpRequest, s, r, rr));
        root.next(session, httpRequest, responseConsumer);
    }

    private void createAndSchedule(final HttpRequest httpRequest,
                                   final Session session,
                                   final Request request,
                                   final Consumer<Response> responseConsumer) {

        final HttpModule httpModule = httpRequest.getManifestMetadata().getModule();
        final HttpOperation httpOperation = httpRequest.getManifestMetadata().getPreferredOperation();

        final Consumer<Throwable> failure = ex -> getExceptionMapperResolver()
            .getExceptionMapper(ex)
            .map(ex, request, responseConsumer);

        final Consumer<Object> success = result -> {
            try {
                final Response response = (Response) result;
                responseConsumer.accept(response);
            } catch (ClassCastException ex) {
                logger.error("Resource did not return Response type.", ex);
                failure.accept(ex);
            }
        };

        try {

            final SimpleRequest simpleRequest = SimpleRequest.builder()
                    .from(request)
                    .parameterizedPath(httpRequest.getManifestMetadata().getPreferredOperation().getPath())
                    .build();

            // Anything that can't be sent over the wire, we will remove to prevent an exception from getting thrown
            // when we attempt to dispatch the request over the network.

            simpleRequest.getAttributes().removeIf((name, value) -> !(value instanceof Serializable));

            getContext().getHandlerContext().invokeSingleUseHandlerAsync(
                success, failure,
                simpleRequest.getAttributes(), httpModule.getModule(),
                httpOperation.getMethod(), request.getPayload(), simpleRequest, session);

        } catch (Throwable th) {
            logRequestFailure(request, th);
            failure.accept(th);
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

    public Filter.Chain.Builder getFilterChainBuilder() {
        return filterChainBuilder;
    }

    @Inject
    public void setFilterChainBuilder(Filter.Chain.Builder filterChainBuilder) {
        this.filterChainBuilder = filterChainBuilder;
    }

    public ExceptionMapper.Resolver getExceptionMapperResolver() {
        return exceptionMapperResolver;
    }

    @Inject
    public void setExceptionMapperResolver(ExceptionMapper.Resolver exceptionMapperResolver) {
        this.exceptionMapperResolver = exceptionMapperResolver;
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(@Named(REMOTE) Context context) {
        this.context = context;
    }

}
