package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static com.namazustudios.socialengine.model.User.USER_ATTRIBUTE;
import static com.namazustudios.socialengine.model.application.Application.APPLICATION_ATTRIUTE;
import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;
import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;
import static com.namazustudios.socialengine.rest.HttpHeaders.SESSION_ID;

@Provider
@PreMatching
public class SessionIdAuthFilter implements ContainerRequestFilter {

    private SessionService sessionService;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String sessionId = requestContext.getHeaderString(SESSION_ID);
        if (sessionId != null) checkSessionAndSetAttributes(requestContext, sessionId);
    }

    private void checkSessionAndSetAttributes(final ContainerRequestContext requestContext, final String sessionId) {

        final Session session = getSessionService().checkSession(sessionId);

        requestContext.setProperty(SESSION_ATTRIBUTE, session);

        final User user = session.getUser();
        final Profile profile = session.getProfile();
        final Application application = session.getApplication();

        if (user != null) requestContext.setProperty(USER_ATTRIBUTE, user);
        if (profile != null) requestContext.setProperty(PROFILE_ATTRIBUTE, profile);
        if (application != null) requestContext.setProperty(APPLICATION_ATTRIUTE, application);

    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

}
