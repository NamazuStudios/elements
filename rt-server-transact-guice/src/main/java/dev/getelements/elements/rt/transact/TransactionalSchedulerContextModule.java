package dev.getelements.elements.rt.transact;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.Scheduler;
import dev.getelements.elements.rt.SchedulerContext;
import dev.getelements.elements.rt.SimpleScheduler;
import dev.getelements.elements.rt.SimpleSchedulerContext;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;

public class TransactionalSchedulerContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(SchedulerContext.class)
                .annotatedWith(named(LOCAL));

        // Transactional Scheduler Context depends on a SimpleSchedulerContext to perform its work, it just adds some
        // support on top of it.

        bind(SimpleSchedulerContext.class)
                .asEagerSingleton();

        bind(SchedulerContext.class)
                .annotatedWith(named(LOCAL))
                .to(TransactionalSchedulerContext.class)
                .asEagerSingleton();
;
    }
}
