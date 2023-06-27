package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.SimpleTaskContext;
import dev.getelements.elements.rt.TaskContext;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;

public class SimpleTaskContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(TaskContext.class)
            .annotatedWith(named(LOCAL));

        bind(TaskContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleTaskContext.class)
            .asEagerSingleton();

    }

}
