package com.namazustudios.promotion.client.rest;

import com.namazustudios.promotion.model.User;
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
    private Client client;

    private User currentUser = User.getUnprivileged();

    @Override
    public void login(String userId, String password, final MethodCallback<User> methodCallback) {
        client.login(userId, password, new MethodCallback<User>() {

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
        client.logout(methodCallback);
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

}
