package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.remote.*;

import java.util.Set;

import static java.util.Collections.singleton;

public class TestWorkerInstanceModule extends PrivateModule {

    @Override
    protected void configure() {

        final Provider<Node> nodeProvider = getProvider(Node.class);
        bind(new TypeLiteral<Set<Node>>(){}).toProvider(() -> singleton(nodeProvider.get()));

        bind(SimpleWorkerInstance.class).asEagerSingleton();
        bind(Worker.class).to(SimpleWorkerInstance.class);
        bind(Instance.class).to(SimpleWorkerInstance.class);

        expose(Worker.class);
        expose(Instance.class);

    }

}
