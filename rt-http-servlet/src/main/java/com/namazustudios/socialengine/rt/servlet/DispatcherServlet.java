package com.namazustudios.socialengine.rt.servlet;

import com.google.common.net.HttpHeaders;
import com.namazustudios.socialengine.rt.ExceptionMapper;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
import com.namazustudios.socialengine.rt.http.HttpManifestMetadata;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.Constants.HTTP_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.Constants.MDC_HTTP_REQUEST;

public class DispatcherServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private HttpRequestService httpRequestService;

    private HttpResponseService httpResponseService;

    private HttpSessionService httpSessionService;

    private SessionRequestDispatcher<HttpRequest> sessionRequestDispatcher;

    private ExceptionMapper.Resolver exceptionMapperResolver;

    private long asyncTimeoutMillisecoinds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mapRequestAndPerformAsync(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mapRequestAndPerformAsync(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mapRequestAndPerformAsync(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mapRequestAndPerformAsync(req, resp);
    }

    @Override
    protected void doOptions(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) throws ServletException, IOException {

        final HttpRequest httpRequest = getHttpRequestService().getRequest(httpServletRequest);
        final HttpManifestMetadata manifestMetadata = httpRequest.getManifestMetadata();

        if (manifestMetadata.hasSinglePreferredOperation()) {
            mapRequestAndPerformAsync(httpServletRequest, httpServletResponse);
        } else {

            final String allow = manifestMetadata.getAvailableOperations()
                .stream()
                .map(o -> o.getVerb().toString())
                .collect(Collectors.joining(","));

            httpServletResponse.addHeader(HttpHeaders.ALLOW, allow);

            manifestMetadata.getAvailableOperations()
                .stream()
                .flatMap(o -> o.getConsumesContentByType().keySet().stream())
                .forEach(ct -> httpServletResponse.addHeader(HttpHeaders.ACCEPT, ct));

            manifestMetadata.getAvailableOperations()
                .stream()
                .flatMap(o -> o.getProducesContentByType().values().stream())
                .flatMap(o -> o.getStaticHeaders() == null ? Stream.empty() : o.getStaticHeaders().entrySet().stream())
                .forEach(e -> httpServletResponse.addHeader(e.getKey(), e.getValue()));

        }

    }

    @Override
    protected void doHead(final HttpServletRequest httpServletRequest,
                          final HttpServletResponse httpServletResponse) throws ServletException, IOException {

        final HttpRequest httpRequest = getHttpRequestService().getRequest(httpServletRequest);
        final HttpManifestMetadata manifestMetadata = httpRequest.getManifestMetadata();

        if (manifestMetadata.hasSinglePreferredOperation()) {
            mapRequestAndPerformAsync(httpServletRequest, httpServletResponse);
        } else {
            super.doHead(httpServletRequest, httpServletResponse);
        }

    }

    private void mapRequestAndPerformAsync(final HttpServletRequest httpServletRequest,
                                           final HttpServletResponse httpServletResponse) {
        getExceptionMapperResolver().performExceptionSafe(
            r -> assembleAndWrite(httpServletRequest, r, httpServletResponse),
            () -> {

                final var asyncContext = httpServletRequest.startAsync();
                asyncContext.setTimeout(getAsyncTimeoutMillisecoinds());

                final var session = getHttpSessionService().getSession(httpServletRequest);
                final var httpRequest = getHttpRequestService().getAsyncRequest(asyncContext);
                final var responseConsumer = getConsumer(asyncContext, httpRequest, httpServletResponse);

                performAsync(httpRequest, session, responseConsumer);

            }
        );
    }

    private Consumer<Response> getConsumer(final AsyncContext asyncContext,
                                           final HttpRequest httpRequest,
                                           final HttpServletResponse httpServletResponse) {

        final String prefix = httpRequest.getId();
        final AtomicBoolean complete = new AtomicBoolean();
        MDC.put(MDC_HTTP_REQUEST, httpRequest.getId());
        logger.debug("{} - Dispatching Request.", prefix);

        asyncContext.addListener(new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                logger.debug("{} - Completed request.", prefix, event.getThrowable());
                complete.set(true);
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                logger.warn("{} - Request timed out.", prefix, event.getThrowable());
                complete.set(true);
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                final HttpServletRequest r = (HttpServletRequest) event.getSuppliedRequest();
                logger.error("{} - Error in async context.", prefix, event.getThrowable());
                complete.set(true);
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                logger.debug("{} - Started AsyncRequest {}", prefix, event.getThrowable());
            }

        });

        return response -> {
            if (!complete.getAndSet(true)) {
                logger.debug("{} - Sending response.", prefix);
                assembleAndWrite(httpRequest, response, httpServletResponse);
                asyncContext.complete();
            } else {
                logger.warn("{} - Request already completed", prefix);
            }
        };

    }

    private void performAsync(final HttpRequest httpRequest,
                              final Session session,
                              final Consumer<Response> responseConsumer) {
        try {
            getSessionRequestDispatcher().dispatch(session, httpRequest, responseConsumer);
        } catch (Exception ex) {
            logger.info("Mapping exception for {} {}", httpRequest.getVerb(), httpRequest.getHeader().getPath());
            getExceptionMapperResolver().getExceptionMapper(ex).map(ex, responseConsumer);
        }
    }

    private void assembleAndWrite(final HttpServletRequest httpServletRequest,
                                  final Response response,
                                  final HttpServletResponse httpServletResponse) {
        try {
            getHttpResponseService().write(httpServletRequest, response, httpServletResponse);
        } catch (Exception ex) {

            logger.error("Caught exception writing normal response.", ex);

            try {
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e) {
                logger.error("Caught exception sending error response.", ex);
            }

        }

    }
    private void assembleAndWrite(final HttpRequest httpRequest,
                                  final Response response,
                                  final HttpServletResponse httpServletResponse) {
        try {
            getHttpResponseService().assembleAndWrite(httpRequest, response, httpServletResponse);
        } catch (Exception ex) {

            logger.error("Caught exception writing normal response.", ex);

            try {
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e) {
                logger.error("Caught exception sending error response.", ex);
            }

        }

    }

    public HttpRequestService getHttpRequestService() {
        return httpRequestService;
    }

    @Inject
    public void setHttpRequestService(HttpRequestService httpRequestService) {
        this.httpRequestService = httpRequestService;
    }

    public HttpResponseService getHttpResponseService() {
        return httpResponseService;
    }

    @Inject
    public void setHttpResponseService(HttpResponseService httpResponseService) {
        this.httpResponseService = httpResponseService;
    }

    public HttpSessionService getHttpSessionService() {
        return httpSessionService;
    }

    @Inject
    public void setHttpSessionService(HttpSessionService httpSessionService) {
        this.httpSessionService = httpSessionService;
    }

    public SessionRequestDispatcher<HttpRequest> getSessionRequestDispatcher() {
        return sessionRequestDispatcher;
    }

    @Inject
    public void setSessionRequestDispatcher(SessionRequestDispatcher<HttpRequest> sessionRequestDispatcher) {
        this.sessionRequestDispatcher = sessionRequestDispatcher;
    }

    public ExceptionMapper.Resolver getExceptionMapperResolver() {
        return exceptionMapperResolver;
    }

    @Inject
    public void setExceptionMapperResolver(ExceptionMapper.Resolver exceptionMapperResolver) {
        this.exceptionMapperResolver = exceptionMapperResolver;
    }

    public long getAsyncTimeoutMillisecoinds() {
        return asyncTimeoutMillisecoinds;
    }

    @Inject
    public void setAsyncTimeoutMillisecoinds(@Named(HTTP_TIMEOUT_MSEC) long asyncTimeoutMillisecoinds) {
        this.asyncTimeoutMillisecoinds = asyncTimeoutMillisecoinds;
    }

}
