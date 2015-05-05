package com.namazustudios.socialengine.rest.provider;

import com.namazustudios.socialengine.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;

/**
 * Manages the {@link com.namazustudios.socialengine.model.User} object and provides it as needed.
 *
 * In the event the {@link com.namazustudios.socialengine.model.User} is not logged in, this returns a default
 * {@link com.namazustudios.socialengine.model.User} with the level set to
 * {@link com.namazustudios.socialengine.model.User.Level#UNPRIVILEGED}
 *
 * Created by patricktwohig on 4/1/15.
 */
public class UserProvider implements Provider<User> {

    public static final String USER_SESSION_KEY = User.class.getName();

    @Inject
    private HttpServletRequest httpServletRequest;

    @Override
    public User get() {

        final HttpSession httpSession = httpServletRequest.getSession(false);

        if (httpSession == null) {
            return User.getUnprivileged();
        }

        final User user = (User) httpServletRequest.getSession().getAttribute(USER_SESSION_KEY);

        if (user == null) {
            return User.getUnprivileged();
        }

        httpServletRequest.setAttribute(USER_SESSION_KEY, user);
        return user;

    }

}
