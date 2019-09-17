package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ClasspathAssetLoader;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;

import java.util.Set;

import static java.util.Collections.singleton;

public class TestApplicationWorkerInstanceModule extends PrivateModule {

    @Override
    protected void configure() {

        final Provider<Node> nodeProvider = getProvider(Node.class);
        bind(new TypeLiteral<Set<Node>>(){}).toProvider(() -> singleton(nodeProvider.get()));

        bind(WorkerInstance.class).asEagerSingleton();
        bind(Worker.class).to(WorkerInstance.class);
        bind(Instance.class).to(WorkerInstance.class);

        expose(Worker.class);
        expose(Instance.class);

    }

}
