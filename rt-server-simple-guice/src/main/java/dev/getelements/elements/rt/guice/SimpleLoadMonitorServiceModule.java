package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.LoadMonitorService;
import dev.getelements.elements.rt.SimpleLoadMonitorService;

public class SimpleLoadMonitorServiceModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(LoadMonitorService.class);
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
    }
}
