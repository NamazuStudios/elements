package dev.getelements.elements.security;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Returns a {@link Supplier<Profile>} which can be used to obtain an instance of {@link Profile}, or throw the
 * appropriate exception type if a {@link Profile} cannot be supplied.  This returns a {@link Supplier<Profile>} to
 * defer the attempt to resolve the {@link Profile} until it is absolutely needed.
 */
public class ProfileSupplierProvider implements Provider<Supplier<Profile>> {

    private Set<ProfileIdentificationMethod> profileIdentificationMethodSet;
    
    @Override
    public Supplier<Profile> get() {
        return () -> {

            for(final ProfileIdentificationMethod method : getProfileIdentificationMethodSet()) {
                try {
                    return method.attempt();
                } catch (UnidentifiedProfileException ex) {
                    continue;
                }
            }

            return ProfileIdentificationMethod.UNIDENTIFIED.attempt();

        };
    }

    public Set<ProfileIdentificationMethod> getProfileIdentificationMethodSet() {
        return profileIdentificationMethodSet;
    }

    @Inject
    public void setProfileIdentificationMethodSet(Set<ProfileIdentificationMethod> profileIdentificationMethodSet) {
        this.profileIdentificationMethodSet = profileIdentificationMethodSet;
    }

}
