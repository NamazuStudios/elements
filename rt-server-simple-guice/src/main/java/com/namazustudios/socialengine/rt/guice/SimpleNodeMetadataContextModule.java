package com.namazustudios.socialengine.rt.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleNodeMetadataContextModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(SimpleNodeMetadataContextModule.class);

    @Override
    protected void configure() {
        bind(NodeMetadataContext.class).to(SimpleNodeMetadataContext.class).asEagerSingleton();

        // TODO: from my understanding, we will have a separate singleton for each Application on an Instance, in which
        //  case we need to move this so that there is exactly one load monitor service per Instance. Could be coupled
        //  with the task to set up a dedicated InstanceMetadataContext.
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
        // TODO: there should also be exactly one Instance UUID Provider per Instance.
        bind(InstanceUuidProvider.class).to(FromDiskInstanceUuidProvider.class).asEagerSingleton();
    }

}
