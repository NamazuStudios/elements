package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.CustomAuthSessionService;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import java.util.regex.Pattern;

import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;
import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;
import static com.namazustudios.socialengine.model.user.User.USER_ATTRIBUTE;
import static java.util.regex.Pattern.compile;

/**
 * Defers to the {@link SessionService} in order to verify a session ID.
 */
public abstract class SessionIdAuthenticationContainerRequestFilter implements ContainerRequestFilter {

    private static final Pattern JWT_PATTERN = compile("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)");

    private SessionService sessionService;

    private CustomAuthSessionService customAuthSessionService;

    /**
     * Checks the session and sets the appropraite attributes to the {@link ContainerRequestContext}.
     *
     * @param requestContext the {@link ContainerRequestContext}
     * @param sessionId the session ID.
     */
    protected void checkSessionAndSetAttributes(final ContainerRequestContext requestContext, final String sessionId) {

        final Session session = isJwt(sessionId) ?
            getCustomAuthSessionService().getSession(sessionId) :
            getSessionService().checkAndRefreshSessionIfNecessary(sessionId);

        setSession(requestContext, session);

    }

    private void setSession(final ContainerRequestContext requestContext, final Session session) {

        requestContext.setProperty(SESSION_ATTRIBUTE, session);

        final User user = session.getUser();
        final Profile profile = session.getProfile();

        if (user != null) requestContext.setProperty(USER_ATTRIBUTE, user);
        if (profile != null) requestContext.setProperty(PROFILE_ATTRIBUTE, profile);

    }

    private Boolean isJwt(final String credentials) {
        return JWT_PATTERN.matcher(credentials).matches();
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
