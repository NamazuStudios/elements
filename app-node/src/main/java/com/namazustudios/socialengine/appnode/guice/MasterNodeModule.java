package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ContextNodeLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterNodeModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(MasterNodeModule.class);

    @Override
    protected void configure() {
        install(new ZContextModule());

        bind(InstanceUuidProvider.class)
            .to(FromDiskInstanceUuidProvider.class)
            .asEagerSingleton();

        bind(NodeLifecycle.class).to(ContextNodeLifecycle.class).asEagerSingleton();
        bind(InvocationDispatcher.class).to(ContextInvocationDispatcher.class);
    }
}
