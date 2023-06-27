package dev.getelements.elements.test.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.SimpleWorkerInstance;
import dev.getelements.elements.rt.remote.Worker;

import java.util.Set;

import static java.util.Collections.singleton;

public class TestWorkerInstanceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(SimpleWorkerInstance.class).asEagerSingleton();
        bind(Worker.class).to(SimpleWorkerInstance.class).asEagerSingleton();
        bind(Instance.class).to(SimpleWorkerInstance.class).asEagerSingleton();

        expose(Worker.class);
        expose(Instance.class);

    }

}
