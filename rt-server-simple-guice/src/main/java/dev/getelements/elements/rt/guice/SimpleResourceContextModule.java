package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.ResourceContext;
import dev.getelements.elements.rt.SimpleResourceContext;
import dev.getelements.elements.rt.remote.provider.CPUCountThreadPoolProvider;

import java.util.concurrent.ExecutorService;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;

public class SimpleResourceContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ResourceContext.class)
            .annotatedWith(named(LOCAL));

        bind(ResourceContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleResourceContext.class)
            .asEagerSingleton();

    }

}
