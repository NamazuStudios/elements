package com.namazustudios.socialengine.servlet.security;

import com.google.common.base.Splitter;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.security.SessionSecretHeader;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.model.User.USER_ATTRIBUTE;
import static com.namazustudios.socialengine.model.application.Application.APPLICATION_ATTRIUTE;
import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;
import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

public class SessionIdAuthenticationFilter implements Filter {

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

        final Optional<String> sessionSecret = new SessionSecretHeader(request::getHeader).getSessionSecret();

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
