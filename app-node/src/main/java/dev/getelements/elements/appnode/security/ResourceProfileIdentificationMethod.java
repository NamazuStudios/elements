package dev.getelements.elements.appnode.security;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Optional;

import static dev.getelements.elements.sdk.model.profile.Profile.PROFILE_ATTRIBUTE;

public class ResourceProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Provider<Resource> resourceProvider;

    @Override
    public Optional<Profile> attempt() {
        return getResourceProvider()
            .get()
            .getAttributes()
            .getAttributeOptional(PROFILE_ATTRIBUTE)
            .map(Profile.class::cast);
    }

    public Provider<Resource> getResourceProvider() {
        return resourceProvider;
    }

    @Inject
    public void setResourceProvider(Provider<Resource> resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

}
