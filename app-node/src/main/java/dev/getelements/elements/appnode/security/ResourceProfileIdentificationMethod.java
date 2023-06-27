package dev.getelements.elements.appnode.security;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.security.ProfileIdentificationMethod;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.model.profile.Profile.PROFILE_ATTRIBUTE;

public class ResourceProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Provider<Resource> resourceProvider;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {
        return getResourceProvider()
            .get()
            .getAttributes()
            .getAttributeOptional(PROFILE_ATTRIBUTE)
            .map(Profile.class::cast)
            .orElseThrow(UnidentifiedProfileException::new);
    }

    public Provider<Resource> getResourceProvider() {
        return resourceProvider;
    }

    @Inject
    public void setResourceProvider(Provider<Resource> resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

}
