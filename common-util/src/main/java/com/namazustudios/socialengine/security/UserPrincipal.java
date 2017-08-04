package com.namazustudios.socialengine.security;

import com.namazustudios.socialengine.model.User;

import java.security.Principal;

/**
 * Created by patricktwohig on 8/4/17.
 */
public class UserPrincipal implements Principal {

    private final User user;

    public UserPrincipal(User user) {

        if (user == null) {
            throw new IllegalArgumentException();
        }

        this.user = user;
    }

    @Override
    public String getName() {
        return user.getName();
    }

}
