package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.LoadMonitorService;
import com.namazustudios.socialengine.rt.SimpleLoadMonitorService;

public class SimpleLoadMonitorServiceModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(LoadMonitorService.class);
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
    }
}
