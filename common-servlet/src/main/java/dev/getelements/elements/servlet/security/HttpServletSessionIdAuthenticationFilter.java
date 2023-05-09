package dev.getelements.elements.servlet.security;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.security.SessionSecretHeader;
import dev.getelements.elements.service.SessionService;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;
import static dev.getelements.elements.model.application.Application.APPLICATION_ATTRIUTE;
import static dev.getelements.elements.model.profile.Profile.PROFILE_ATTRIBUTE;
import static dev.getelements.elements.model.session.Session.SESSION_ATTRIBUTE;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

public class HttpServletSessionIdAuthenticationFilter implements Filter {

    private SessionService sessionService;

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

        final Optional<String> sessionSecret = SessionSecretHeader.withValueSupplier(request::getHeader)
                                                                  .getSessionSecret();

        sessionSecret.ifPresent(ss -> {

            final Session session;

            try {
                session = getSessionService().checkAndRefreshSessionIfNecessary(ss);
            } catch (final ForbiddenException ex) {
                response.setStatus(SC_FORBIDDEN);
                return;
            }

            final User user = session.getUser();
            final Profile profile = getProfile(request, session);
            final Application application = session.getApplication();

            request.setAttribute(SESSION_ATTRIBUTE, session);
            if (user != null) request.setAttribute(USER_ATTRIBUTE, user);
            if (profile != null) request.setAttribute(PROFILE_ATTRIBUTE, profile);
            if (application != null) request.setAttribute(APPLICATION_ATTRIUTE, application);

        });

        chain.doFilter(request, response);

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

}
