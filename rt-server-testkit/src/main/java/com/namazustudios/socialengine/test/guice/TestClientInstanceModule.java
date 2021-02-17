package com.namazustudios.socialengine.test.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.SimpleInstance;

public class TestClientInstanceModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(Instance.class).to(SimpleInstance.class).asEagerSingleton();
        expose(Instance.class);
    }

}
