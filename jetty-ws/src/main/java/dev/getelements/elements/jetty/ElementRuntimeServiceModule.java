package dev.getelements.elements.jetty;

import com.google.inject.PrivateModule;
import dev.getelements.elements.common.app.ElementRuntimeService;
import dev.getelements.elements.common.app.StandardElementRuntimeService;

/**
 * Guice module for {@link ElementRuntimeService}.
 */
public class ElementRuntimeServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ElementRuntimeService.class);

        bind(ElementRuntimeService.class)
                .to(StandardElementRuntimeService.class)
                .asEagerSingleton();

    }

}
