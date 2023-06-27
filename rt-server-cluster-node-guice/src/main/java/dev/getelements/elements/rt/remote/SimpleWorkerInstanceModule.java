package dev.getelements.elements.rt.remote;

import com.google.inject.PrivateModule;

public class SimpleWorkerInstanceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(SimpleWorkerInstance.class).asEagerSingleton();

        bind(Worker.class).to(SimpleWorkerInstance.class);
        bind(Instance.class).to(SimpleWorkerInstance.class);

        expose(Worker.class);
        expose(Instance.class);

    }

}
