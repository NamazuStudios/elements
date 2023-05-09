package dev.getelements.elements.rt.xodus;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.SchedulerContext;
import dev.getelements.elements.rt.SimpleSchedulerContext;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;

public class XodusSchedulerContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(SchedulerContext.class)
            .annotatedWith(named(LOCAL));

        // Xodus Scheduler Context depends on a SimpleSchedulerContext to perform its work, it just adds some
        // support on top of it.

        bind(SimpleSchedulerContext.class)
            .asEagerSingleton();

        bind(SchedulerContext.class)
            .annotatedWith(named(LOCAL))
            .to(XodusSchedulerContext.class)
            .asEagerSingleton();

    }

}
