package com.namazustudios.socialengine.rt.provider;

import com.namazustudios.socialengine.rt.CurrentResource;
import com.namazustudios.socialengine.rt.Resource;

import javax.inject.Provider;

public class CurrentResourceProvider implements Provider<Resource> {

    @Override
    public Resource get() {
        return CurrentResource.getInstance().getCurrent();
    }

}
