package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.SimpleInstance;

public class SimpleInstanceModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(SimpleInstance.class).asEagerSingleton();
        bind(Instance.class).to(SimpleInstance.class);
        expose(Instance.class);
    }

}
