package com.namazustudios.socialengine.appnode.provider;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.model.user.User.USER_ATTRIBUTE;

public class ResourceUserAuthenticationMethod implements UserAuthenticationMethod {

    private Provider<Resource> resourceProvider;

    @Override
    public User attempt() throws ForbiddenException {
        return getResourceProvider()
            .get()
            .getAttributes()
            .getAttributeOptional(USER_ATTRIBUTE)
            .map(User.class::cast)
            .orElseThrow(ForbiddenException::new);
    }

    public Provider<Resource> getResourceProvider() {
        return resourceProvider;
    }

    @Inject
    public void setResourceProvider(Provider<Resource> resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

}
