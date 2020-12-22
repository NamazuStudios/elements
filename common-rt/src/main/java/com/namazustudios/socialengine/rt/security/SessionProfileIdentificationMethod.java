package com.namazustudios.socialengine.rt.security;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;

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
