package dev.getelements.elements.appnode.security;

import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.rt.Resource;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Optional;

import static dev.getelements.elements.sdk.model.session.Session.SESSION_ATTRIBUTE;

public class ResourceOptionalSessionProvider implements Provider<Optional<Session>> {

    private Provider<Resource> resourceProvider;

    @Override
    public Optional<Session> get() {
        return getResourceProvider()
            .get()
            .getAttributes()
            .getAttributeOptional(SESSION_ATTRIBUTE)
            .map(Session.class::cast);
    }

    public Provider<Resource> getResourceProvider() {
        return resourceProvider;
    }

    @Inject
    public void setResourceProvider(Provider<Resource> resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

}
