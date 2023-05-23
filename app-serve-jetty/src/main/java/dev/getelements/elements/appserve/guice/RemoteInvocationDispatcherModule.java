package dev.getelements.elements.appserve.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.remote.RemoteInvocationDispatcher;
import dev.getelements.elements.rt.remote.SimpleRemoteInvocationDispatcher;

public class RemoteInvocationDispatcherModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(RemoteInvocationDispatcher.class)
            .to(SimpleRemoteInvocationDispatcher.class)
            .asEagerSingleton();

        expose(RemoteInvocationDispatcher.class);

    }

}
