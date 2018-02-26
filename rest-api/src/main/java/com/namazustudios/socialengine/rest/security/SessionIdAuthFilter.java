package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;

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

    private SessionDao sessionDao;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        final String sessionId = requestContext.getHeaderString(SESSION_ID);

        if (sessionId != null) {

            final Session session = getSessionDao().getBySessionId(sessionId);

            requestContext.setProperty(SESSION_ATTRIBUTE, session);

            final User user = session.getUser();
            final Profile profile = session.getProfile();
            final Application application = session.getApplication();

            if (user != null) requestContext.setProperty(USER_ATTRIBUTE, user);
            if (profile != null) requestContext.setProperty(PROFILE_ATTRIBUTE, profile);
            if (application != null) requestContext.setProperty(APPLICATION_ATTRIUTE, application);

        }

    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

}
