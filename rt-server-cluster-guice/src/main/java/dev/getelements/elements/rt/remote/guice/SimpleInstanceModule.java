package dev.getelements.elements.rt.remote.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.SimpleInstance;

public class SimpleInstanceModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(Instance.class).to(SimpleInstance.class).asEagerSingleton();
        expose(Instance.class);
    }

}
