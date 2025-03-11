package dev.getelements.elements.rt.remote;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ControlClientProvider implements Provider<ControlClient> {

    private Provider<ControlClient.Factory> controlClientFactoryProvider;

    private Provider<InstanceConnectionService> instanceConnectionServiceProvider;

    @Override
    public ControlClient get() {
        final var factory = getControlClientFactoryProvider().get();
        final var service = getInstanceConnectionServiceProvider().get();
        return factory.open(service.getLocalControlAddress());
    }

    public Provider<ControlClient.Factory> getControlClientFactoryProvider() {
        return controlClientFactoryProvider;
    }

    @Inject
    public void setControlClientFactoryProvider(Provider<ControlClient.Factory> controlClientFactoryProvider) {
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
