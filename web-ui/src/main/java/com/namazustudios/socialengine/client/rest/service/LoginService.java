package com.namazustudios.socialengine.client.rest.service;

import com.namazustudios.socialengine.client.rest.client.internal.LoginClient;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.SessionCreation;
import org.fusesource.restygwt.client.MethodCallback;

/**
 * Sits on top of the {@link LoginClient} and tracks the clients logged in state.
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
     * Refreshs the current user.  If the user
     * @param userMethodCallback
     */
    void refreshCurrentUser(final MethodCallback<User> userMethodCallback);

    /**
     * Gets the currently logged-in user.  If no user is logged in, this will return {@link User#getUnprivileged()} to
     * indicate so.
     *
     * @return the currently logged-in user
     */
    default User getCurrentUser() {
        return getSessionCreation() == null ? User.getUnprivileged() : getSessionCreation().getSession().getUser();
    }

    /**
     * Returns the current {@link }
     * @return
     */
    SessionCreation getSessionCreation();

}
