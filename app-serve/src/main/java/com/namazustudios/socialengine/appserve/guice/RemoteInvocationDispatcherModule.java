package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.RemoteInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.SimpleRemoteInvocationDispatcher;

public class RemoteInvocationDispatcherModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(RemoteInvocationDispatcher.class)
            .to(SimpleRemoteInvocationDispatcher.class)
            .asEagerSingleton();

        expose(RemoteInvocationDispatcher.class);

    }

}
