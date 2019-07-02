package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.SimpleTaskContext;
import com.namazustudios.socialengine.rt.TaskContext;

public class SimpleTaskContextModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(TaskContext.class);
        bind(TaskContext.class).to(SimpleTaskContext.class);
    }

}
