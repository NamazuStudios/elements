package com.namazustudios.socialengine.rt.security;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

public class SessionUserAuthenticationMethod implements UserAuthenticationMethod {

    private Provider<Optional<Session>> optionalSessionProvider;

    @Override
    public User attempt() throws ForbiddenException {
        return getOptionalSessionProvider()
            .get()
            .map(session -> session.getUser())
            .orElseThrow(ForbiddenException::new);
    }

    public Provider<Optional<Session>> getOptionalSessionProvider() {
        return optionalSessionProvider;
    }

    @Inject
    public void setOptionalSessionProvider(Provider<Optional<Session>> optionalSessionProvider) {
        this.optionalSessionProvider = optionalSessionProvider;
    }

}
