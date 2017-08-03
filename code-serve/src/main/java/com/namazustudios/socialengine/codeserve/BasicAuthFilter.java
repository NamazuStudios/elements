package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.security.AuthorizationHeader;
import com.namazustudios.socialengine.service.AuthService;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class BasicAuthFilter implements Filter {

    /**
     * Constant for the WWW-Authenticate header.
     */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    private AuthService authService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

//        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setHeader(WWW_AUTHENTICATE, AuthorizationHeader.AUTH_TYPE_BASIC);
        filterChain.doFilter(servletRequest, servletResponse);

        if (httpServletResponse.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
            httpServletResponse.setHeader(WWW_AUTHENTICATE, AuthorizationHeader.AUTH_TYPE_BASIC);
        }

    }

    @Override
    public void destroy() {}

    public AuthService getAuthService() {
        return authService;
    }

    @Inject
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

}
