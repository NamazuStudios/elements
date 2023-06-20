package dev.getelements.elements.servlet.security;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.UnauthorizedException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.security.SessionSecretHeader;
import dev.getelements.elements.service.CustomAuthSessionService;
import dev.getelements.elements.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

import static dev.getelements.elements.Headers.BEARER;
import static dev.getelements.elements.Headers.WWW_AUTHENTICATE;
import static dev.getelements.elements.model.application.Application.APPLICATION_ATTRIUTE;
import static dev.getelements.elements.model.profile.Profile.PROFILE_ATTRIBUTE;
import static dev.getelements.elements.model.session.Session.SESSION_ATTRIBUTE;
import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;
import static java.util.regex.Pattern.compile;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class HttpServletSessionIdAuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletSessionIdAuthenticationFilter.class);

    private static final Pattern JWT_PATTERN = compile("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)");

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

            SessionSecretHeader.withValueSupplier(request::getHeader)
                    .getSessionSecret()
                    .ifPresent(sessionId -> checkSessionAndSetAttributes(sessionId, request, response, chain));

            chain.doFilter(request, response);

        } catch (ForbiddenException ex) {
            response.setStatus(SC_FORBIDDEN);
        } catch (UnauthorizedException ex) {
            response.setStatus(SC_UNAUTHORIZED);
            response.setHeader(WWW_AUTHENTICATE, BEARER);
        } catch (Exception ex) {
            logger.error("Caught exception processing security credentials.", ex);
            chain.doFilter(request, response);
        }

    }

    private void checkSessionAndSetAttributes(final String sessionId,
                                              final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final FilterChain chain) {

        final var session = isJwt(sessionId) ?
                getCustomAuthSessionService().getSession(sessionId) :
                getSessionService().checkAndRefreshSessionIfNecessary(sessionId);

        final var user = session.getUser();
        final var profile = getProfile(request, session);
        final var application = session.getApplication();

        request.setAttribute(SESSION_ATTRIBUTE, session);
        if (user != null) request.setAttribute(USER_ATTRIBUTE, user);
        if (profile != null) request.setAttribute(PROFILE_ATTRIBUTE, profile);
        if (application != null) request.setAttribute(APPLICATION_ATTRIUTE, application);

    }

    private Boolean isJwt(final String credentials) {
        return JWT_PATTERN.matcher(credentials).matches();
    }

    private Profile getProfile(final HttpServletRequest request, final Session session) {
        return session.getProfile();
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
