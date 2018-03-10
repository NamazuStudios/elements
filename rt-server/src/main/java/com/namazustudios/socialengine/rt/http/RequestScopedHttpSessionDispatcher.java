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

    private Context context;

    @Override
    public void dispatch(final Session session,
                         final HttpRequest httpRequest,
                         final Consumer<Response> responseConsumer) {

        final Filter.Chain chain;
        chain = Filter.Chain.build(getFilterList(), (s, r, rr) -> createAndSchedule(httpRequest, s, r, rr));

        final Request request = SimpleRequest.builder()
            .from(httpRequest)
            .parameterizedPath(httpRequest.getManifestMetadata().getPreferredOperation().getPath())
            .build();

        chain.next(session, request, responseConsumer);

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
                logger.error("Resource did not return Response type.");
                failure.accept(ex);
            }
        };

        try {
            getContext().getHandlerContext().invokeRemoteHandlerAsync(
                success, failure,
                request.getAttributes(), httpModule.getModule(),
                httpOperation.getMethod(), request.getPayload(), request, session);
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

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

}
