package dev.getelements.elements.security;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Optional;
import java.util.Set;

public class ProfileOptionalSupplier implements Provider<Optional<Profile>> {

    private Set<ProfileIdentificationMethod> profileIdentificationMethodSet;

    @Override
    public Optional<Profile> get() {
        return getProfileIdentificationMethodSet()
                .stream()
                .map(ProfileIdentificationMethod::attempt)
                .filter(Optional::isPresent)
                .findFirst()
                .orElseGet(Optional::empty);
    }

    public Set<ProfileIdentificationMethod> getProfileIdentificationMethodSet() {
        return profileIdentificationMethodSet;
    }

    @Inject
    public void setProfileIdentificationMethodSet(Set<ProfileIdentificationMethod> profileIdentificationMethodSet) {
        this.profileIdentificationMethodSet = profileIdentificationMethodSet;
    }

}
