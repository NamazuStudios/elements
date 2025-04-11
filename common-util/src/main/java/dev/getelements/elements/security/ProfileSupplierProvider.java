package dev.getelements.elements.security;

import dev.getelements.elements.sdk.model.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.sdk.model.profile.Profile;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Returns a {@link Supplier<Profile>} which can be used to obtain an instance of {@link Profile}, or throw the
 * appropriate exception type if a {@link Profile} cannot be supplied.  This returns a {@link Supplier<Profile>} to
 * defer the attempt to resolve the {@link Profile} until it is absolutely needed.
 */
public class ProfileSupplierProvider implements Provider<Supplier<Profile>> {

    private Optional<Profile> optionalProfile;

    @Override
    public Supplier<Profile> get() {
        return () -> getOptionalProfile().orElseThrow(UnidentifiedProfileException::new);
    }

    public Optional<Profile> getOptionalProfile() {
        return optionalProfile;
    }

    @Inject
    public void setOptionalProfile(Optional<Profile> optionalProfile) {
        this.optionalProfile = optionalProfile;
    }

}
