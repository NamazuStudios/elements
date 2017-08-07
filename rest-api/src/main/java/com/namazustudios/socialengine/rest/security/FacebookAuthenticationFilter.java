package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.exception.AuthorizationHeaderParseException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.FacebookSession;
import com.namazustudios.socialengine.security.AuthorizationHeader;
import com.namazustudios.socialengine.security.FacebookAuthorizationHeader;
import com.namazustudios.socialengine.service.FacebookAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static com.namazustudios.socialengine.security.AuthorizationHeader.AUTH_TYPE_FACEBOOK;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

/**
 * Created by patricktwohig on 6/26/17.
 */
@Provider
@PreMatching
public class FacebookAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FacebookAuthenticationFilter.class);

    private FacebookAuthService facebookAuthService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final String authorizationHeaderValue = requestContext.getHeaderString(AUTHORIZATION);

        if (authorizationHeaderValue == null) {
            return;
        }

        try {

            final AuthorizationHeader authorizationHeader = new AuthorizationHeader(authorizationHeaderValue);

            if (AUTH_TYPE_FACEBOOK.equals(authorizationHeader.getType())) {

                final FacebookAuthorizationHeader facebookAuthorizationHeader;
                facebookAuthorizationHeader = authorizationHeader.asFacebookAuthHeader();

                final FacebookSession facebookSession = getFacebookSession(facebookAuthorizationHeader);
                requestContext.setProperty(User.USER_ATTRIBUTE, facebookSession.getUser());
                requestContext.setProperty(Profile.PROFILE_ATTRIBUTE, facebookSession.getProfile());
                requestContext.setProperty(Application.APPLICATION_ATTRIUTE, facebookSession.getApplication());

            }

        } catch (AuthorizationHeaderParseException ex) {
            logger.info("Bad request.  Failing silently: {}", ex.getMessage());
        }

    }

    private FacebookSession getFacebookSession(final FacebookAuthorizationHeader facebookAuthorizationHeader) {
        final String applicationId = facebookAuthorizationHeader.getApplicationId();
        final String userAccessToken = facebookAuthorizationHeader.getAccessToken();
        return getFacebookAuthService().authenticate(applicationId, userAccessToken);
    }

    public FacebookAuthService getFacebookAuthService() {
        return facebookAuthService;
    }

    @Inject
    public void setFacebookAuthService(FacebookAuthService facebookAuthService) {
        this.facebookAuthService = facebookAuthService;
    }

}
