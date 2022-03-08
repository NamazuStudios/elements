package com.namazustudios.socialengine.rt.remote;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.rt.remote.watchdog.*;

public class SimpleWatchdogServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        final var multibinder = Multibinder.newSetBinder(binder(), WorkerWatchdog.class);

        multibinder.addBinding().to(NodeBindingWatchdog.class);
        multibinder.addBinding().to(NodeHealthWorkerWatchdog.class);

        bind(WatchdogService.class).to(SimpleWatchdogService.class);
        expose(WatchdogService.class);

    }

}
