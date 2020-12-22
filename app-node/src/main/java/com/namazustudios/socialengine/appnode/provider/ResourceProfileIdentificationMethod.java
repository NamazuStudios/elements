package com.namazustudios.socialengine.appnode.provider;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;

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
