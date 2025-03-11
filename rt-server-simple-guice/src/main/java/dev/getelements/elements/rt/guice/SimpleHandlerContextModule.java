package dev.getelements.elements.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.HandlerContext;
import dev.getelements.elements.rt.SimpleHandlerContext;

import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;
import static dev.getelements.elements.rt.HandlerContext.HANDLER_TIMEOUT_MSEC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SimpleHandlerContextModule extends PrivateModule {

    private Runnable bindTimeout = () -> {};

    /**
     * Specifies the {@link HandlerContext} timeout to the default values.
     */
    public SimpleHandlerContextModule withDefaultTimeout() {
        return withTimeout(60, SECONDS);
    }

    /**
     * Specifies the {@link HandlerContext} timeout.
     *
     * @param duration the duration
     * @param sourceUnits the units
     */
    public SimpleHandlerContextModule withTimeout(final long duration, final TimeUnit sourceUnits) {
        bindTimeout = () -> bind(Long.class)
            .annotatedWith(named(HANDLER_TIMEOUT_MSEC))
            .toInstance(MILLISECONDS.convert(duration, sourceUnits));
        return this;
    }

    @Override
    protected void configure() {

        bindTimeout.run();

        expose(HandlerContext.class)
            .annotatedWith(named(LOCAL));

        bind(HandlerContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleHandlerContext.class)
            .asEagerSingleton();

    }

}
