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

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DispatcherServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private HttpRequestService httpRequestService;

    private HttpResponseService httpResponseService;

    private HttpSessionService httpSessionService;

    private SessionRequestDispatcher<HttpRequest> sessionRequestDispatcher;

    private ExceptionMapper.Resolver exceptionMapperResolver;

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

//        final HttpRequest httpRequest = getHttpRequestService().getRequest(httpServletRequest);
//        final HttpManifestMetadata manifestMetadata = httpRequest.getManifestMetadata();
//
//        if (manifestMetadata.hasSinglePreferredOperation()) {
//            try {
//                final HttpRequest asyncHttpServletRequest = getHttpRequestService().getAsyncRequest(httpServletRequest);
//                performAsync(asyncHttpServletRequest, httpServletRequest.getAsyncContext());
//            } catch (Exception ex) {
//                logger.info("Mapping exception for {} {}", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
//
//                getExceptionMapperResolver()
//                        .getExceptionMapper(ex)
//                        .map(ex, response -> assembleAndWrite(httpRequest, response, httpServletResponse));
//
//                logger.info("Mapped exception properly.", ex);
//            }
//        } else {
//
//            final String allow = manifestMetadata.getAvailableOperations()
//                .stream()
//                .map(o -> o.getVerb().toString())
//                .collect(Collectors.joining(","));
//
//            httpServletResponse.addHeader(HttpHeaders.ALLOW, allow);
//
//            manifestMetadata.getAvailableOperations()
//                .stream()
//                .flatMap(o -> o.getConsumesContentByType().keySet().stream())
//                .forEach(ct -> httpServletResponse.addHeader(HttpHeaders.ACCEPT, ct));
//
//            manifestMetadata.getAvailableOperations()
//                .stream()
//                .flatMap(o -> o.getProducesContentByType().values().stream())
//                .flatMap(o -> o.getStaticHeaders().entrySet().stream())
//                .forEach(e -> httpServletResponse.addHeader(e.getKey(), e.getValue()));
//
//        }

    }

    @Override
    protected void doHead(final HttpServletRequest httpServletRequest,
                          final HttpServletResponse httpServletResponse) throws ServletException, IOException {

//        final HttpRequest httpRequest = getHttpRequestService().getRequest(httpServletRequest);
//
//        if (httpRequest.getManifestMetadata().hasSinglePreferredOperation()) {
//            try {
//                final HttpRequest asyncHttpServletRequest = getHttpRequestService().getAsyncRequest(httpServletRequest);
//                performAsync(asyncHttpServletRequest, httpServletRequest.getAsyncContext());
//            } catch (Exception ex) {
//
//                logger.info("Mapping exception for {} {}", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
//
//                getExceptionMapperResolver()
//                        .getExceptionMapper(ex)
//                        .map(ex, response -> assembleAndWrite(httpRequest, response, httpServletResponse));
//
//                httpServletRequest.getAsyncContext().complete();
//                logger.info("Mapped exception properly.", ex);
//
//            }
//        } else {
//            super.doHead(httpServletRequest, httpServletResponse);
//        }

    }

    private void mapRequestAndPerformAsync(final HttpServletRequest httpServletRequest,
                                           final HttpServletResponse httpServletResponse) {

        final AsyncContext asyncContext = httpServletRequest.startAsync();

        final Session session = getHttpSessionService().getSession(httpServletRequest);
        final HttpRequest httpRequest = getHttpRequestService().getAsyncRequest(asyncContext);

        final Consumer<Response> responseConsumer = response -> {
            assembleAndWrite(httpRequest, response, httpServletResponse);
            asyncContext.complete();
        };

        performAsync(httpRequest, session, responseConsumer);

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

}
