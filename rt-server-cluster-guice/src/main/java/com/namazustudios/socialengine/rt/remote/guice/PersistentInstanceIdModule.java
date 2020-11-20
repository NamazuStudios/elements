package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.PersistentInstanceIdProvider;

public class PersistentInstanceIdModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceId.class).toProvider(PersistentInstanceIdProvider.class).asEagerSingleton();
    }

}
