package com.namazustudios.promotion.client.rest;

import com.google.gwt.user.client.Cookies;
import com.namazustudios.promotion.model.User;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/1/15.
 */
public class UserProvider implements Provider<User> {

    @Inject
    private LoginService loginService;

    @Override
    public User get() {
        return loginService.getCurrentUser();
    }

}
