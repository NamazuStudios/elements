package com.namazustudios.socialengine.client.rest.service;

import com.namazustudios.socialengine.client.rest.client.LoginClient;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by patricktwohig on 5/1/15.
 */
@Singleton
public class RestyLoginService implements LoginService {

    @Inject
    private LoginClient loginClient;

    @Inject
    private UserClient userClient;

    private User currentUser = User.getUnprivileged();

    @Override
    public void login(String userId, String password, final MethodCallback<User> methodCallback) {
        loginClient.login(userId, password, new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                currentUser = User.getUnprivileged();
                methodCallback.onFailure(method, throwable);
            }

            @Override
            public void onSuccess(Method method, User user) {
                currentUser = user;
                methodCallback.onSuccess(method, user);
            }

        });
    }

    @Override
    public void logout(MethodCallback<Void> methodCallback) {
        currentUser = User.getUnprivileged();
        loginClient.logout(methodCallback);
    }

    @Override
    public void refreshCurrentUser(final MethodCallback<User> userMethodCallback) {
        userClient.refreshCurrentUser(new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                userMethodCallback.onFailure(method, throwable);
            }

            @Override
            public void onSuccess(Method method, User user) {
                currentUser = user;
                userMethodCallback.onSuccess(method, user);
            }

        });
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

}
