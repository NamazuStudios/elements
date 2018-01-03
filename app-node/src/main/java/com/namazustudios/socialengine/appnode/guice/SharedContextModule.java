package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.guice.SimpleIndexContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleSchedulerContextModule;

public class SharedContextModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new SimpleIndexContextModule());
        install(new SimpleSchedulerContextModule());
    }

}
