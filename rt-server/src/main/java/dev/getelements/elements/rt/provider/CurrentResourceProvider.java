package dev.getelements.elements.rt.provider;

import dev.getelements.elements.rt.CurrentResource;
import dev.getelements.elements.rt.Resource;

import jakarta.inject.Provider;

public class CurrentResourceProvider implements Provider<Resource> {

    @Override
    public Resource get() {
        return CurrentResource.getInstance().getCurrent();
    }

}
