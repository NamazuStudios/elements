package com.namazustudios.promotion.rest.provider;

import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.exception.UnauthorizedException;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.AuthService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * Manages the {@link com.namazustudios.promotion.model.User} object and provides it as needed.
 *
 * In the event the {@link com.namazustudios.promotion.model.User} is not logged in, this returns a default
 * {@link com.namazustudios.promotion.model.User} with the level set to
 * {@link com.namazustudios.promotion.model.User.Level#UNPRIVILEGED}
 *
 * Created by patricktwohig on 4/1/15.
 */
public class UserProvider implements Provider<User> {

    public static final String USER_SESSION_KEY = User.class.getName();

    @Inject
    private HttpHeaders httpHeaders;

    @Inject
    private HttpServletRequest httpServletRequest;

    @Override
    public User get() {

        User user = (User)httpServletRequest.getSession().getAttribute(USER_SESSION_KEY);

        if (user == null) {
            user = new User();
            user.setName("");
            user.setEmail("");
            user.setLevel(User.Level.UNPRIVILEGED);
        }

        httpServletRequest.setAttribute(USER_SESSION_KEY, user);

        return user;

    }

}
