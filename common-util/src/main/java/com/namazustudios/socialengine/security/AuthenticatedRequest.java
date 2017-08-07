package com.namazustudios.socialengine.security;

import com.namazustudios.socialengine.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

import static com.namazustudios.socialengine.model.User.USER_ATTRIBUTE;

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
