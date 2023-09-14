package dev.getelements.elements.security;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;
import java.util.Set;

public class ProfileOptionalSupplier implements Provider<Optional<Profile>> {

    private Set<ProfileIdentificationMethod> profileIdentificationMethodSet;

    @Override
    public Optional<Profile> get() {

        for(final ProfileIdentificationMethod method : getProfileIdentificationMethodSet()) {
            try {
                return Optional.of(method.attempt());
            } catch (UnidentifiedProfileException ex) {
                continue;
            }
        }

        return Optional.empty();

    }

    public Set<ProfileIdentificationMethod> getProfileIdentificationMethodSet() {
        return profileIdentificationMethodSet;
    }

    @Inject
    public void setProfileIdentificationMethodSet(Set<ProfileIdentificationMethod> profileIdentificationMethodSet) {
        this.profileIdentificationMethodSet = profileIdentificationMethodSet;
    }

}
