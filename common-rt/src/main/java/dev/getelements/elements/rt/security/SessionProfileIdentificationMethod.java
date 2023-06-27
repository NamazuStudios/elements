package dev.getelements.elements.rt.security;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.security.ProfileIdentificationMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

public class SessionProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Provider<Optional<Session>> optionalSessionProvider;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {
        return getOptionalSessionProvider()
            .get()
            .map(Session::getProfile)
            .orElseThrow(UnidentifiedProfileException::new);
    }

    public Provider<Optional<Session>> getOptionalSessionProvider() {
        return optionalSessionProvider;
    }

    @Inject
    public void setOptionalSessionProvider(Provider<Optional<Session>> optionalSessionProvider) {
        this.optionalSessionProvider = optionalSessionProvider;
    }

}
