package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.HandlerContext;
import com.namazustudios.socialengine.rt.SimpleHandlerContext;

public class SimpleHandlerContextModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HandlerContext.class).to(SimpleHandlerContext.class);
    }

}
