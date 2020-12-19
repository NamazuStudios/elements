package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import javax.inject.Provider;

public class ResourceAttributesProvider implements Provider<Attributes> {

    private Resource resource;

    @Override
    public Attributes get() {
        return getResource().getAttributes();
    }

    public Resource getResource() {
        return resource;
    }

    @Inject
    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
