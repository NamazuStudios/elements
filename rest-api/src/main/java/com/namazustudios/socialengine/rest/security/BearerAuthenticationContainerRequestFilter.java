package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.security.AuthorizationHeader;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

@Provider
@PreMatching
public class BearerAuthenticationContainerRequestFilter extends SessionIdAuthenticationContainerRequestFilter {

    private static final Pattern JWT_PATTERN = compile("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)");

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        AuthorizationHeader.withValueSupplier(requestContext::getHeaderString)
            .map(AuthorizationHeader::asBearerHeader)
            .ifPresent(h -> {
                if (isJwt(h.getCredentials())) {
                    checkJWTAndSetAttributes(requestContext, h.getCredentials());
                } else {
                    checkSessionAndSetAttributes(requestContext, h.getCredentials());
                }
            });
    }

    private Boolean isJwt(final String credentials) {
        return JWT_PATTERN.matcher(credentials).matches();
    }

}
