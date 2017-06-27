package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.FacebookAuthService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static com.namazustudios.socialengine.rest.XHttpHeaders.AUTH_TYPE_FACEBOOK;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by patricktwohig on 6/26/17.
 */
@Provider
@PreMatching
public class FacebookAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = getLogger(FacebookAuthenticationFilter.class);

    private FacebookAuthService facebookAuthService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final String authorizationHeaderValue = requestContext.getHeaderString(AUTHORIZATION);

        if (authorizationHeaderValue == null) {
            return;
        }

        final AuthorizationHeader authorizationHeader = new AuthorizationHeader(authorizationHeaderValue);

        if (AUTH_TYPE_FACEBOOK.equals(authorizationHeader.getType())) {

            final FacebookAuthorizationHeader facebookAuthorizationHeader;
            facebookAuthorizationHeader = authorizationHeader.asFacebookAuthHeader();

            final User user = getUser(facebookAuthorizationHeader);
            requestContext.setProperty(User.USER_ATTRIBUTE, user);

        }

    }

    private User getUser(final FacebookAuthorizationHeader facebookAuthorizationHeader) {
        final String applicationId = facebookAuthorizationHeader.getApplicationId();
        final String userAccessToken = facebookAuthorizationHeader.getAccessToken();
        return getFacebookAuthService().authenticateUser(applicationId, userAccessToken);
    }

    public FacebookAuthService getFacebookAuthService() {
        return facebookAuthService;
    }

    @Inject
    public void setFacebookAuthService(FacebookAuthService facebookAuthService) {
        this.facebookAuthService = facebookAuthService;
    }

}
