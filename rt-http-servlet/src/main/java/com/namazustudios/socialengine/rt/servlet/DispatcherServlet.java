package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.ExceptionMapper;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DispatcherServlet extends HttpServlet {

    private HttpRequestService httpRequestService;

    private HttpResponseService httpResponseService;

    private HttpSessionService httpSessionService;

    private SessionRequestDispatcher sessionRequestDispatcher;

    private ExceptionMapper<Throwable> throwableExceptionMapper;

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    private void perform() {

        try {

        } catch (Throwable th) {

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

    public SessionRequestDispatcher getSessionRequestDispatcher() {
        return sessionRequestDispatcher;
    }

    @Inject
    public void setSessionRequestDispatcher(SessionRequestDispatcher sessionRequestDispatcher) {
        this.sessionRequestDispatcher = sessionRequestDispatcher;
    }

    public ExceptionMapper<Throwable> getThrowableExceptionMapper() {
        return throwableExceptionMapper;
    }

    @Inject
    public void setThrowableExceptionMapper(ExceptionMapper<Throwable> throwableExceptionMapper) {
        this.throwableExceptionMapper = throwableExceptionMapper;
    }

}
