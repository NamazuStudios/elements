package dev.getelements.elements.deployment.jetty.guice;

import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.deployment.jetty.JettyElementContainerService;
import dev.getelements.elements.deployment.jetty.StandardElementRuntimeService;
import dev.getelements.elements.deployment.jetty.loader.AuthFilterFeature;
import dev.getelements.elements.deployment.jetty.loader.JakartaRsLoader;
import dev.getelements.elements.deployment.jetty.loader.JakartaWebsocketLoader;
import dev.getelements.elements.deployment.jetty.loader.Loader;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.getelements.elements.servlet.security.HttpServletBearerAuthenticationFilter;
import dev.getelements.elements.servlet.security.HttpServletElementScopeFilter;
import dev.getelements.elements.servlet.security.HttpServletHeaderProfileOverrideFilter;
import dev.getelements.elements.servlet.security.HttpServletSessionIdAuthenticationFilter;
import jakarta.servlet.Filter;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;

/**
 * Element module for deployment services. Registers ElementRuntimeService and ElementContainerService
 * implementations as first-class Elements within the sdk.deployment package.
 */
public class JettySdkElementModule extends SharedElementModule {

    public JettySdkElementModule() {
        super("dev.getelements.elements.deployment.jetty");
    }

    @Override
    protected void configureElement() {

        bind(ElementRuntimeService.class)
                .to(StandardElementRuntimeService.class)
                .asEagerSingleton();

        bind(ElementContainerService.class)
                .to(JettyElementContainerService.class)
                .asEagerSingleton();

        final var loaders = newSetBinder(binder(), Loader.class);
        loaders.addBinding().to(JakartaRsLoader.class);
        loaders.addBinding().to(JakartaWebsocketLoader.class);

        final var  authFilters = Multibinder.newSetBinder(
                binder(),
                Filter.class,
                named(AuthFilterFeature.FILTER_SET)
        );

        authFilters.addBinding().to(HttpServletElementScopeFilter.class);
        authFilters.addBinding().to(HttpServletBearerAuthenticationFilter.class);
        authFilters.addBinding().to(HttpServletSessionIdAuthenticationFilter.class);
        authFilters.addBinding().to(HttpServletHeaderProfileOverrideFilter.class);

    }

}
