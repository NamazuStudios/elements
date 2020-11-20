package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlClient;
import org.zeromq.ZContext;

import javax.inject.Provider;

public class JeroMQControlClientModule extends AbstractModule {

    @Override
    protected void configure() {
        final Provider<ZContext> zContextProvider = getProvider(ZContext.class);
        bind(ControlClient.Factory.class).toInstance(connectAddress -> new JeroMQControlClient(zContextProvider.get(), connectAddress));
    }

}
