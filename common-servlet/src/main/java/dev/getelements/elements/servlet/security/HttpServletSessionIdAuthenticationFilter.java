package dev.getelements.elements.servlet.security;

import dev.getelements.elements.security.SessionSecretHeader;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class HttpServletSessionIdAuthenticationFilter extends HttpServletAuthenticationFilter {

    @Override
    protected Optional<String> getAuthToken(HttpServletRequest request) {
        return SessionSecretHeader.withValueSupplier(request::getHeader).getSessionSecret();
    }

}
