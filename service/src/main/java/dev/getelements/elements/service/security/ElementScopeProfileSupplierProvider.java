package dev.getelements.elements.service.security;

import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.profile.Profile;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Optional;
import java.util.function.Supplier;

public class ElementScopeProfileSupplierProvider implements Provider<Supplier<Profile>> {

    private Provider<Optional<Profile>> optionalProfileProvider;

    @Override
    public Supplier<Profile> get() {
        return () -> getOptionalProfileProvider()
                .get()
                .orElseThrow(() -> new ProfileNotFoundException("No current profile."));
    }

    public Provider<Optional<Profile>> getOptionalProfileProvider() {
        return optionalProfileProvider;
    }

    @Inject
    public void setOptionalProfileProvider(Provider<Optional<Profile>> optionalProfileProvider) {
        this.optionalProfileProvider = optionalProfileProvider;
    }

}
