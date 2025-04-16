package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.util.security.AuthorizationHeader;
import dev.getelements.elements.sdk.util.security.BearerAuthorizationHeader;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public class HttpServletBearerAuthenticationFilter extends HttpServletAuthenticationFilter {

    @Override
    protected Optional<String> getAuthToken(HttpServletRequest request) {
        return AuthorizationHeader
                .withValueSupplier(request::getHeader)
                .map(AuthorizationHeader::asBearerHeader)
                .map(BearerAuthorizationHeader::getCredentials);
    }

}
