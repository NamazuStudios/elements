package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.HandlerContext;
import com.namazustudios.socialengine.rt.SimpleHandlerContext;

import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleHandlerContextModule extends AbstractModule {

    private Runnable bindTimeout = () -> {};

    /**
     * Specifies the {@link HandlerContext} timeout
     *
     * @param duration
     * @param sourceUnits
     */
    public SimpleHandlerContextModule withTimeout(final long duration, final TimeUnit sourceUnits) {
        bindTimeout = () -> bind(Long.class)
            .annotatedWith(named(HandlerContext.HANDLER_TIMEOUT_MSEC))
            .toInstance(MILLISECONDS.convert(duration, sourceUnits));
        return this;
    }

    @Override
    protected void configure() {
        bindTimeout.run();
        bind(HandlerContext.class).to(SimpleHandlerContext.class);
    }

}
