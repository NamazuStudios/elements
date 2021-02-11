package com.namazustudios.socialengine.servlet.security;

import com.namazustudios.socialengine.exception.security.AuthorizationHeaderParseException;
import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.UnauthorizedException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.AuthenticatedRequest;
import com.namazustudios.socialengine.security.AuthorizationHeader;
import com.namazustudios.socialengine.security.BasicAuthorizationHeader;
import com.namazustudios.socialengine.service.UsernamePasswordAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.namazustudios.socialengine.exception.StatusMapping.map;
import static com.namazustudios.socialengine.security.AuthorizationHeader.AUTH_TYPE_BASIC;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class HttpServletBasicAuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletBasicAuthFilter.class);

    /**
     * Constant for the WWW-Authenticate header.
     */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    private UsernamePasswordAuthService usernamePasswordAuthService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        try {
            final HttpServletRequest authorized = authorize(httpServletRequest);
            filterChain.doFilter(authorized, servletResponse);
        } catch (UnauthorizedException ex) {
            final int status = map(ex);
            httpServletResponse.setHeader(WWW_AUTHENTICATE, AUTH_TYPE_BASIC);
            httpServletResponse.setStatus(status);
            logger.info("Request unauthorized.  Specifying auth type {} in {}", AUTH_TYPE_BASIC, WWW_AUTHENTICATE);
        } catch (BaseException ex) {
            final int status = map(ex);
            httpServletResponse.setStatus(status);
            logger.info("Request failed ex: {}", ex.getCode(), ex);
        } catch (Exception ex) {
            logger.error("Internal Error", ex);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private HttpServletRequest authorize(final HttpServletRequest httpServletRequest) {

        final String authHeaderValue = httpServletRequest.getHeader(AuthorizationHeader.AUTH_HEADER);

        if (authHeaderValue == null) {
            throw new UnauthorizedException();
        }

        final AuthorizationHeader authorizationHeader;
        final BasicAuthorizationHeader basicAuthHeader;

        try {
            authorizationHeader = new AuthorizationHeader(authHeaderValue);
            basicAuthHeader = authorizationHeader.asBasicHeader(httpServletRequest.getCharacterEncoding());
        } catch (AuthorizationHeaderParseException ex) {
            throw new ForbiddenException(ex);
        }

        final User user = getUsernamePasswordAuthService().loginUser(basicAuthHeader.getUsername(), basicAuthHeader.getPassword());
        httpServletRequest.setAttribute(User.USER_ATTRIBUTE, user);

        return new AuthenticatedRequest(httpServletRequest, authorizationHeader);

    }

    @Override
    public void destroy() {}

    public UsernamePasswordAuthService getUsernamePasswordAuthService() {
        return usernamePasswordAuthService;
    }

    @Inject
    public void setUsernamePasswordAuthService(UsernamePasswordAuthService usernamePasswordAuthService) {
        this.usernamePasswordAuthService = usernamePasswordAuthService;
    }

}
