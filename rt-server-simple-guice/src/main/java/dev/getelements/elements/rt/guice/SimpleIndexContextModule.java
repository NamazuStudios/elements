package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.IndexContext;
import dev.getelements.elements.rt.SimpleIndexContext;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;

public class SimpleIndexContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(IndexContext.class).annotatedWith(named(LOCAL));

        bind(IndexContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleIndexContext.class)
            .asEagerSingleton();

    }

}
