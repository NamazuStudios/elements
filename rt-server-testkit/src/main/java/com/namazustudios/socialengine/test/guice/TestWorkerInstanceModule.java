package com.namazustudios.socialengine.test.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.SimpleWorkerInstance;
import com.namazustudios.socialengine.rt.remote.Worker;

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
