package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.SimpleTaskContext;
import com.namazustudios.socialengine.rt.TaskContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;

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
