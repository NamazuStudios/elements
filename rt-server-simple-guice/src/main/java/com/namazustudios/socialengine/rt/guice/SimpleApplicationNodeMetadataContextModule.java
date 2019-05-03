package com.namazustudios.socialengine.rt.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.ApplicationNodeMetadataContext;
import com.namazustudios.socialengine.rt.LoadMonitorService;
import com.namazustudios.socialengine.rt.SimpleApplicationNodeMetadataContext;
import com.namazustudios.socialengine.rt.SimpleLoadMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleApplicationNodeMetadataContextModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(SimpleApplicationNodeMetadataContextModule.class);

    @Override
    protected void configure() {
        bind(ApplicationNodeMetadataContext.class).to(SimpleApplicationNodeMetadataContext.class).asEagerSingleton();
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
    }

}
