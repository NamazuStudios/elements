package dev.getelements.elements.security;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.util.security.AuthorizationHeader;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

import static dev.getelements.elements.sdk.model.user.User.USER_ATTRIBUTE;

/**
 * Created by patricktwohig on 8/4/17.
 */
public class AuthenticatedRequest extends HttpServletRequestWrapper {

    private final AuthorizationHeader authorizationHeader;

    public AuthenticatedRequest(
            final HttpServletRequest request,
            final AuthorizationHeader authorizationHeader) {
        super(request);
        this.authorizationHeader = authorizationHeader;
    }

    @Override
    public String getAuthType() {
        return authorizationHeader.getType();
    }

    @Override
    public String getRemoteUser() {
        final User user = (User) getAttribute(USER_ATTRIBUTE);
        return user == null || user.getName() == null ? super.getRemoteUser() : user.getName();
    }

    @Override
    public Principal getUserPrincipal() {
        final User user = (User) getAttribute(USER_ATTRIBUTE);
        return user == null ? super.getUserPrincipal() : new UserPrincipal(user);
    }

}
