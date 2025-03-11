package dev.getelements.elements.app.serve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.spi.ProvisionListener;
import dev.getelements.elements.app.serve.JettyApplicationDeploymentService;
import dev.getelements.elements.app.serve.loader.JakartaRsLoader;
import dev.getelements.elements.app.serve.loader.JakartaWebsocketLoader;
import dev.getelements.elements.app.serve.loader.Loader;
import dev.getelements.elements.common.app.ApplicationDeploymentService;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.common.app.ApplicationDeploymentService.APP_SERVE;

public class AppServeModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(ApplicationDeploymentService.class)
                .annotatedWith(named(APP_SERVE))
                .to(JettyApplicationDeploymentService.class)
                .asEagerSingleton();

        final var loaders = newSetBinder(binder(), Loader.class);
        loaders.addBinding().to(JakartaRsLoader.class);
        loaders.addBinding().to(JakartaWebsocketLoader.class);

        final var serviceKey = Key.get(JettyApplicationDeploymentService.class);

        bindListener(
                b -> b.getKey().equals(serviceKey),
                new ProvisionListener() {
                    @Override
                    public <T> void onProvision(final ProvisionInvocation<T> provision) {
                        final var service = (ApplicationDeploymentService) provision.provision();
                        service.start();
                    }
                });

    }

}
