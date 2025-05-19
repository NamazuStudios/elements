package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.model.Headers;
import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.security.AuthenticatedRequest;
import dev.getelements.elements.sdk.util.security.AuthorizationHeader;
import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.exception.StatusMapping.map;
import static dev.getelements.elements.sdk.util.security.AuthorizationHeader.AUTH_TYPE_BASIC;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class HttpServletBasicAuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletBasicAuthFilter.class);

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
            httpServletResponse.setHeader(Headers.WWW_AUTHENTICATE, AUTH_TYPE_BASIC);
            httpServletResponse.setStatus(status);
            logger.info("Request unauthorized.  Specifying auth type {} in {}", AUTH_TYPE_BASIC, Headers.WWW_AUTHENTICATE);
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

        final var authHeaderValue = httpServletRequest.getHeader(AuthorizationHeader.AUTH_HEADER);

        if (authHeaderValue == null) {
            throw new UnauthorizedException();
        }

        final var authorizationHeader = new AuthorizationHeader(authHeaderValue);
        final var basicAuthHeader = authorizationHeader.asBasicHeader(httpServletRequest.getCharacterEncoding());

        final var user = getUsernamePasswordAuthService().createSession(basicAuthHeader)
                .getSession()
                .getUser();

        httpServletRequest.setAttribute(User.USER_ATTRIBUTE, user);
        return new AuthenticatedRequest(httpServletRequest, authorizationHeader);

    }

    @Override
    public void destroy() {}

    public UsernamePasswordAuthService getUsernamePasswordAuthService() {
        return usernamePasswordAuthService;
    }

    @Inject
    public void setUsernamePasswordAuthService(@Named(UNSCOPED) UsernamePasswordAuthService usernamePasswordAuthService) {
        this.usernamePasswordAuthService = usernamePasswordAuthService;
    }

}
