package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.User;
import org.slf4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static com.namazustudios.socialengine.rest.XHttpHeaders.AUTH_TYPE_FACEBOOK;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by patricktwohig on 6/26/17.
 */
@Provider
public class FacebookAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = getLogger(FacebookAuthenticationFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final String authorizationHeaderValue = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

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
        // TODO Implement
        return null;
    }

}
