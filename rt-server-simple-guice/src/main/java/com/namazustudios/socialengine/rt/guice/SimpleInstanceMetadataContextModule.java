package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.SimpleInstanceMetadataContext;

public class SimpleInstanceMetadataContextModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceMetadataContext.class).to(SimpleInstanceMetadataContext.class).asEagerSingleton();
    }

}
