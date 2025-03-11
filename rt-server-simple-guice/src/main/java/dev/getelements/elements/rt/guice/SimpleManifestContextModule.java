package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.ManifestContext;
import dev.getelements.elements.rt.SimpleManifestContext;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;

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
