package dev.getelements.elements.rt.security;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Optional;

public class SessionProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Provider<Optional<Session>> optionalSessionProvider;

    @Override
    public Optional<Profile> attempt() {
        return getOptionalSessionProvider()
            .get()
            .map(Session::getProfile);
    }

    public Provider<Optional<Session>> getOptionalSessionProvider() {
        return optionalSessionProvider;
    }

    @Inject
    public void setOptionalSessionProvider(Provider<Optional<Session>> optionalSessionProvider) {
        this.optionalSessionProvider = optionalSessionProvider;
    }

}
