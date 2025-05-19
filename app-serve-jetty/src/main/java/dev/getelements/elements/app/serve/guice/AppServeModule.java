package dev.getelements.elements.app.serve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.app.serve.JettyApplicationDeploymentService;
import dev.getelements.elements.app.serve.loader.AuthFilterFeature;
import dev.getelements.elements.app.serve.loader.JakartaRsLoader;
import dev.getelements.elements.app.serve.loader.JakartaWebsocketLoader;
import dev.getelements.elements.app.serve.loader.Loader;
import dev.getelements.elements.common.app.ApplicationDeploymentService;
import dev.getelements.elements.servlet.security.HttpServletBearerAuthenticationFilter;
import dev.getelements.elements.servlet.security.HttpServletElementScopeFilter;
import dev.getelements.elements.servlet.security.HttpServletHeaderProfileOverrideFilter;
import dev.getelements.elements.servlet.security.HttpServletSessionIdAuthenticationFilter;
import jakarta.servlet.Filter;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.common.app.ApplicationDeploymentService.APP_SERVE;

public class AppServeModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(ApplicationDeploymentService.class)
                .annotatedWith(named(APP_SERVE))
                .to(JettyApplicationDeploymentService.class)
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

        expose(ApplicationDeploymentService.class).annotatedWith(named(APP_SERVE));

    }

}
