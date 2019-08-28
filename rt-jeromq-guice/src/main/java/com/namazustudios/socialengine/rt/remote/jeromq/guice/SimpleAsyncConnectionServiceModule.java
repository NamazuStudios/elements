package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.jeromq.SimpleAsyncConnectionService;
import org.zeromq.ZContext;

public class SimpleAsyncConnectionServiceModule extends PrivateModule {

    @Override
    protected void configure() {
        requireBinding(ZContext.class);
        bind(AsyncConnectionService.class).to(SimpleAsyncConnectionService.class).asEagerSingleton();
        expose(AsyncConnectionService.class);
    }

}
