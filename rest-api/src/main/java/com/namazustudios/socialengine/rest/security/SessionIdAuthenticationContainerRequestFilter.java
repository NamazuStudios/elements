package com.namazustudios.socialengine.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.JWTCredentials;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;
import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;
import static com.namazustudios.socialengine.model.user.User.USER_ATTRIBUTE;

/**
 * Defers to the {@link SessionService} in order to verify a session ID.
 */
public abstract class SessionIdAuthenticationContainerRequestFilter implements ContainerRequestFilter {

    private SessionService sessionService;

    private UserService userService;

    private ObjectMapper objectMapper;

    /**
     * Checks the session and sets the appropraite attributes to the {@link ContainerRequestContext}.
     *
     * @param requestContext the {@link ContainerRequestContext}
     * @param sessionId the session ID.
     */
    protected void checkSessionAndSetAttributes(final ContainerRequestContext requestContext, final String sessionId) {

        final Session session = getSessionService().checkAndRefreshSessionIfNecessary(sessionId);

        requestContext.setProperty(SESSION_ATTRIBUTE, session);

        final User user = session.getUser();
        final Profile profile = session.getProfile();

        if (user != null) requestContext.setProperty(USER_ATTRIBUTE, user);
        if (profile != null) requestContext.setProperty(PROFILE_ATTRIBUTE, profile);

    }

    protected void checkJWTAndSetAttributes(final ContainerRequestContext requestContext, final String jwt) {
        var jwtCredentials = new JWTCredentials(jwt);

        // TODO verify the signature against private key
        var signature = jwtCredentials.getSignature();

        var elm_uesrKey = jwtCredentials.getClaim("elm_userkey");
        var elm_user = getUserService().getUser(elm_uesrKey);

        if (elm_user == null)
        {
            // create user
        }

        requestContext.setProperty(USER_ATTRIBUTE, elm_user);
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public UserService getUserService() { return userService; }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
