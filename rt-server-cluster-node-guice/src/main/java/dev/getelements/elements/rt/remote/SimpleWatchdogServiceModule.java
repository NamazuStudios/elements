package dev.getelements.elements.rt.remote;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.rt.remote.watchdog.*;

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
