package dev.getelements.elements.test.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.SimpleInstance;

public class TestClientInstanceModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(Instance.class).to(SimpleInstance.class).asEagerSingleton();
        expose(Instance.class);
    }

}
