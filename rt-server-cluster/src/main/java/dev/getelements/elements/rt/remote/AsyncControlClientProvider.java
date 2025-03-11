package dev.getelements.elements.rt.remote;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class AsyncControlClientProvider implements Provider<AsyncControlClient> {

    private Provider<AsyncControlClient.Factory> controlClientFactoryProvider;

    private Provider<InstanceConnectionService> instanceConnectionServiceProvider;

    @Override
    public AsyncControlClient get() {
        final var factory = getControlClientFactoryProvider().get();
        final var service = getInstanceConnectionServiceProvider().get();
        return factory.open(service.getLocalControlAddress());
    }

    public Provider<AsyncControlClient.Factory> getControlClientFactoryProvider() {
        return controlClientFactoryProvider;
    }

    @Inject
    public void setControlClientFactoryProvider(Provider<AsyncControlClient.Factory> controlClientFactoryProvider) {
        this.controlClientFactoryProvider = controlClientFactoryProvider;
    }

    public Provider<InstanceConnectionService> getInstanceConnectionServiceProvider() {
        return instanceConnectionServiceProvider;
    }

    @Inject
    public void setInstanceConnectionServiceProvider(Provider<InstanceConnectionService> instanceConnectionServiceProvider) {
        this.instanceConnectionServiceProvider = instanceConnectionServiceProvider;
    }

}
