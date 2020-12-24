package com.namazustudios.socialengine.appnode.security;

import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.Resource;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Optional;

import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;

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
