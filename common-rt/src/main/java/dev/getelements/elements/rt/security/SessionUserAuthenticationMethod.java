package dev.getelements.elements.rt.security;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.UserAuthenticationMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

public class SessionUserAuthenticationMethod implements UserAuthenticationMethod {

    private Provider<Optional<Session>> optionalSessionProvider;

    @Override
    public User attempt() throws ForbiddenException {
        return getOptionalSessionProvider()
            .get()
            .map(Session::getUser)
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
