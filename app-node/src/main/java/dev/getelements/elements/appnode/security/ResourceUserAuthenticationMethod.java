package dev.getelements.elements.appnode.security;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.security.UserAuthenticationMethod;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;

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
