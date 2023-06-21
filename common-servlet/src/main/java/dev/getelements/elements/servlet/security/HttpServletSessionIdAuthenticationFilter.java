package dev.getelements.elements.servlet.security;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.UnauthorizedException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.security.JWTCredentials;
import dev.getelements.elements.security.SessionSecretHeader;
import dev.getelements.elements.service.CustomAuthSessionService;
import dev.getelements.elements.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static dev.getelements.elements.Headers.BEARER;
import static dev.getelements.elements.Headers.WWW_AUTHENTICATE;
import static dev.getelements.elements.model.application.Application.APPLICATION_ATTRIUTE;
import static dev.getelements.elements.model.profile.Profile.PROFILE_ATTRIBUTE;
import static dev.getelements.elements.model.session.Session.SESSION_ATTRIBUTE;
import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;
import static java.util.regex.Pattern.compile;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class HttpServletSessionIdAuthenticationFilter extends HttpServletAuthenticationFilter {

    @Override
    protected Optional<String> getAuthToken(HttpServletRequest request) {
        return SessionSecretHeader.withValueSupplier(request::getHeader).getSessionSecret();
    }

}
