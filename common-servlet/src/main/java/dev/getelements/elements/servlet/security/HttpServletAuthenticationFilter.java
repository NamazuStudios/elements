package dev.getelements.elements.servlet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.exception.BaseException;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.UnauthorizedException;
import dev.getelements.elements.model.ErrorResponse;
import dev.getelements.elements.security.JWTCredentials;
import dev.getelements.elements.service.CustomAuthSessionService;
import dev.getelements.elements.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static dev.getelements.elements.Headers.BEARER;
import static dev.getelements.elements.Headers.WWW_AUTHENTICATE;
import static dev.getelements.elements.exception.ErrorCode.UNKNOWN;
import static dev.getelements.elements.model.application.Application.APPLICATION_ATTRIBUTE;
import static dev.getelements.elements.model.profile.Profile.PROFILE_ATTRIBUTE;
import static dev.getelements.elements.model.session.Session.SESSION_ATTRIBUTE;
import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;
import static javax.servlet.http.HttpServletResponse.*;

public abstract class HttpServletAuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletAuthenticationFilter.class);

    private ObjectMapper objectMapper;

    private SessionService sessionService;

    private CustomAuthSessionService customAuthSessionService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(final ServletRequest _request,
                         final ServletResponse _response,
                         final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) _request;
        final HttpServletResponse response = (HttpServletResponse) _response;

        try {
            getAuthToken(request).ifPresent(sid -> checkSessionAndSetAttributes(sid, request, response, chain));
            chain.doFilter(request, response);
        } catch (ForbiddenException ex) {
            response.setStatus(SC_FORBIDDEN);
            fail(response, ex);
        } catch (UnauthorizedException ex) {
            response.setStatus(SC_UNAUTHORIZED);
            response.setHeader(WWW_AUTHENTICATE, BEARER);
            fail(response, ex);
        } catch (Exception ex) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            fail(response, ex);
            logger.error("Caught exception processing security credentials.", ex);
        }

    }

    private void fail(final HttpServletResponse response, final Exception ex) throws IOException {
        final var error = new ErrorResponse();
        error.setCode(UNKNOWN.toString());
        error.setMessage(ex.getMessage());
        response.setContentType("application/json");
        getObjectMapper().writeValue(response.getOutputStream(), error);
    }

    private void fail(final HttpServletResponse response, final BaseException ex) throws IOException {
        final var error = new ErrorResponse();
        error.setCode(ex.getCode().toString());
        error.setMessage(ex.getMessage());
        response.setContentType("application/json");
        getObjectMapper().writeValue(response.getOutputStream(), error);
    }

    protected abstract Optional<String> getAuthToken(final HttpServletRequest request);

    private void checkSessionAndSetAttributes(final String sessionId,
                                              final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final FilterChain chain) {

        final var session = JWTCredentials.isJwt(sessionId) ?
                getCustomAuthSessionService().getSession(sessionId) :
                getSessionService().checkAndRefreshSessionIfNecessary(sessionId);

        final var user = session.getUser();
        final var profile = session.getProfile();
        final var application = session.getApplication();

        request.setAttribute(SESSION_ATTRIBUTE, session);
        if (user != null) request.setAttribute(USER_ATTRIBUTE, user);
        if (profile != null) request.setAttribute(PROFILE_ATTRIBUTE, profile);
        if (application != null) request.setAttribute(APPLICATION_ATTRIBUTE, application);

    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public CustomAuthSessionService getCustomAuthSessionService() {
        return customAuthSessionService;
    }

    @Inject
    public void setCustomAuthSessionService(CustomAuthSessionService customAuthSessionService) {
        this.customAuthSessionService = customAuthSessionService;
    }

}
