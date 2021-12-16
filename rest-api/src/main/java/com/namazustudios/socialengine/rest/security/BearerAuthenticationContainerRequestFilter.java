package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.security.AuthorizationHeader;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class BearerAuthenticationContainerRequestFilter extends SessionIdAuthenticationContainerRequestFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        AuthorizationHeader.withValueSupplier(requestContext::getHeaderString)
            .map(AuthorizationHeader::asBearerHeader)
            .ifPresent(h -> {
                if (ValidateJWTFormat(h.getCredentials())) {
                    checkJWTAndSetAttributes(requestContext, h.getCredentials());
                } else {
                    checkSessionAndSetAttributes(requestContext, h.getCredentials());
                }
            });
    }

    // check for JWT format {xxx.xxx.xxx}
    private Boolean ValidateJWTFormat(String credentials) {
        return credentials.matches("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)");
    }
}
