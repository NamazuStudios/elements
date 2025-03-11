package dev.getelements.elements.sdk.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.ServiceLocator;

/**
 * Bridges the gap between a Guice {@link com.google.inject.Injector} and an {@link ServiceLocator}.
 */
public class GuiceServiceLocatorModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(ServiceLocator.class).to(GuiceServiceLocator.class);
        expose(ServiceLocator.class);
    }

}
