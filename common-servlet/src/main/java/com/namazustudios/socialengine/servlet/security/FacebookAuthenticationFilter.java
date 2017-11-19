package com.namazustudios.socialengine.servlet.security;

import com.namazustudios.socialengine.exception.AuthorizationHeaderParseException;
import com.namazustudios.socialengine.exception.BaseException;
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
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.namazustudios.socialengine.exception.StatusMapping.map;
import static com.namazustudios.socialengine.security.AuthorizationHeader.AUTH_TYPE_FACEBOOK;

public class FacebookAuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FacebookAuthenticationFilter.class);

    private FacebookAuthService facebookAuthService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) _request;
        final HttpServletResponse response = (HttpServletResponse) _response;

        try {
            setUserIfAvailable(request);
            chain.doFilter(request, response);
        } catch (BaseException ex) {
            logger.info("Caught exception servicing the request.", ex);
            final int status = map(ex);
            response.setStatus(status);
        }

    }

    private void setUserIfAvailable(final HttpServletRequest request) {

        final String authorizationHeaderValue = request.getHeader(AuthorizationHeader.AUTH_HEADER);

        if (authorizationHeaderValue == null) {
            return;
        }

        try {

            final AuthorizationHeader authorizationHeader = new AuthorizationHeader(authorizationHeaderValue);

            if (AUTH_TYPE_FACEBOOK.equals(authorizationHeader.getType())) {

                final FacebookAuthorizationHeader facebookAuthorizationHeader;
                facebookAuthorizationHeader = authorizationHeader.asFacebookAuthHeader();

                final FacebookSession facebookSession = getFacebookSession(facebookAuthorizationHeader);
                request.setAttribute(User.USER_ATTRIBUTE, facebookSession.getUser());
                request.setAttribute(Profile.PROFILE_ATTRIBUTE, facebookSession.getProfile());
                request.setAttribute(Application.APPLICATION_ATTRIUTE, facebookSession.getApplication());

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

    @Override
    public void destroy() {}

    public FacebookAuthService getFacebookAuthService() {
        return facebookAuthService;
    }

    @Inject
    public void setFacebookAuthService(FacebookAuthService facebookAuthService) {
        this.facebookAuthService = facebookAuthService;
    }

}
