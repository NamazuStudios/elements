package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.ManifestContext;
import com.namazustudios.socialengine.rt.SimpleManifestContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;

public class SimpleManifestContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ManifestContext.class)
            .annotatedWith(named(LOCAL));

        bind(ManifestContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleManifestContext.class)
            .asEagerSingleton();

    }

}
