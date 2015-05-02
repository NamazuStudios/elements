package com.namazustudios.promotion.client.rest;

import com.namazustudios.promotion.model.User;
import org.fusesource.restygwt.client.MethodCallback;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Sits on top of the {@link Client} and tracks the clients logged in state.
 *
 * Created by patricktwohig on 5/1/15.
 */
public interface LoginService {

    /**
     * Logs the current user in.
     *
     * @param userId the userId
     * @param password the password
     * @param methodCallback the MethodCallback
     */
    void login(final String userId, final String password, final MethodCallback<User> methodCallback);

    /**
     * Logs the current user out.  This attempts to make a logout, but in all cases
     * will abandon any instances of the currently logged-in user.
     *
     * @param methodCallback
     */
    void logout(final MethodCallback<Void> methodCallback);

    /**
     * Gets the currently logged-in user;
     * @return
     */
    User getCurrentUser();

}
