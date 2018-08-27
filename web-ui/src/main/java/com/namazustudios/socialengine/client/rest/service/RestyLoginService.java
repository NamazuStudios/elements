package com.namazustudios.socialengine.client.rest.service;

import com.namazustudios.socialengine.client.rest.client.internal.LoginClient;
import com.namazustudios.socialengine.client.rest.client.internal.UserClient;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
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

    private SessionCreation sessionCreation;

    @Override
    public void login(final String userId, final String password, final MethodCallback<User> methodCallback) {

        final UsernamePasswordSessionRequest usernamePasswordSessionRequest = new UsernamePasswordSessionRequest();
        usernamePasswordSessionRequest.setUserId(userId);
        usernamePasswordSessionRequest.setPassword(password);

        loginClient.login(usernamePasswordSessionRequest, new MethodCallback<SessionCreation>() {

            @Override
            public void onFailure(final Method method, final Throwable throwable) {
                RestyLoginService.this.sessionCreation = null;
                methodCallback.onFailure(method, throwable);
            }

            @Override
            public void onSuccess(final Method method, final SessionCreation sessionCreation) {
                RestyLoginService.this.sessionCreation = sessionCreation;
                methodCallback.onSuccess(method, sessionCreation.getSession().getUser());
            }

        });

    }

    @Override
    public void logout(MethodCallback<Void> methodCallback) {
        sessionCreation = null;
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
                sessionCreation.getSession().setUser(user);
                userMethodCallback.onSuccess(method, user);
            }

        });
    }

    @Override
    public SessionCreation getSessionCreation() {
        return sessionCreation;
    }

}
