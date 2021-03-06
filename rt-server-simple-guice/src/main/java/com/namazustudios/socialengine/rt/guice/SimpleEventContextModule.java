package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.EventContext;
import com.namazustudios.socialengine.rt.SimpleEventContext;
import com.namazustudios.socialengine.rt.SimpleEventService;

import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;
import static com.namazustudios.socialengine.rt.EventContext.EVENT_TIMEOUT_MSEC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SimpleEventContextModule extends PrivateModule {

    private Runnable bindTimeout = () -> {};

    /**
     * Specifies the {@link SimpleEventService} timeout to the default values.
     */
    public SimpleEventContextModule withDefaultTimeout() {
        return withTimeout(60, SECONDS);
    }

    /**
     * Specifies the {@link SimpleEventService} timeout.
     *
     * @param duration the duration
     * @param sourceUnits the units
     */
    public SimpleEventContextModule withTimeout(final long duration, final TimeUnit sourceUnits) {
        bindTimeout = () -> bind(Long.class)
            .annotatedWith(named(EVENT_TIMEOUT_MSEC))
            .toInstance(MILLISECONDS.convert(duration, sourceUnits));
        return this;
    }

    @Override
    protected void configure() {

        bindTimeout.run();

        expose(EventContext.class)
            .annotatedWith(named(LOCAL));

        bind(EventContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleEventContext.class)
            .asEagerSingleton();

    }

}
