package com.namazustudios.socialengine.rest.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserClaim;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.security.JWTCredentials;
import com.namazustudios.socialengine.security.JWTClaims;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.UserService;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import java.util.HashMap;

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

    private Mapper mapper;

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

    /**
     * Checks the JWT and sets the appropraite attributes to the {@link ContainerRequestContext}.
     *
     * @param requestContext the {@link ContainerRequestContext}
     * @param jwt token
     */
    protected void checkJWTAndSetAttributes(final ContainerRequestContext requestContext, final String jwt) {

        final var jwtCredentials = new JWTCredentials(jwt);

        if (!jwtCredentials.verify()) {
            throw new BadRequestException();
        }

        var uesrKey = jwtCredentials.getClaim(JWTClaims.ELM_USERKEY);
        var elm_user = getUserService().getUser(uesrKey);

        if (elm_user == null)
        {

            var elm_userModelString = jwtCredentials.getClaim("");

            if (elm_userModelString == null) {
                throw new BadRequestException();
            }

            try {

                var elm_userModel = objectMapper.readValue("", UserClaim.class);

                // create user
                var toCreate = new UserCreateRequest();
                toCreate.setName(elm_userModel.getName());
                toCreate.setEmail(elm_userModel.getEmail());
                toCreate.setLevel(elm_userModel.getLevel());

                var createUserResponse = getUserService().createUser(toCreate);
                elm_user = getMapper().map(createUserResponse, User.class);

            } catch (JsonProcessingException e) {
                throw new BadRequestException(e);
            }
        }

        requestContext.setProperty(USER_ATTRIBUTE, elm_user);
        // TODO: How to get profile from user?

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

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

}
