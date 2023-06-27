package dev.getelements.elements.security;

import dev.getelements.elements.exception.security.SessionNotFoundException;
import dev.getelements.elements.model.session.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

public class SessionProvider implements Provider<Session> {

    private Provider<Optional<Session>> optionalSessionProvider;

    @Override
    public Session get() {
        return getOptionalSessionProvider().get().orElseThrow(SessionNotFoundException::new);
    }

    public Provider<Optional<Session>> getOptionalSessionProvider() {
        return optionalSessionProvider;
    }

    @Inject
    public void setOptionalSessionProvider(Provider<Optional<Session>> optionalSessionProvider) {
        this.optionalSessionProvider = optionalSessionProvider;
    }

}
