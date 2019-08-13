package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.id.InstanceId;

public class RandomInstanceIdModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceId.class).toProvider(InstanceId::new).asEagerSingleton();
    }

}
