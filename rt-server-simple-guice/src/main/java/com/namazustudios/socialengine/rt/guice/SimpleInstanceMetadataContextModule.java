package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.LoadMonitorService;
import com.namazustudios.socialengine.rt.SimpleInstanceMetadataContext;
import com.namazustudios.socialengine.rt.SimpleLoadMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleInstanceMetadataContextModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceMetadataContext.class).to(SimpleInstanceMetadataContext.class);
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
    }

}
