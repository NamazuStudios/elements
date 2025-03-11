package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.SchedulerContext;
import dev.getelements.elements.rt.SimpleSchedulerContext;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Constants.SCHEDULER_THREADS;
import static dev.getelements.elements.rt.Context.LOCAL;

public class SimpleSchedulerContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(SchedulerContext.class)
            .annotatedWith(named(LOCAL));

        bind(SchedulerContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleSchedulerContext.class)
            .asEagerSingleton();

    }

}
