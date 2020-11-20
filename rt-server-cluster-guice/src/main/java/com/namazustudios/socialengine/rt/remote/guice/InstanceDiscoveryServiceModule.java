package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.InstanceDiscoveryService;

import java.util.Properties;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.Constants.INSTANCE_DISCOVERY_SERVICE;

public class InstanceDiscoveryServiceModule extends PrivateModule {

    private final Supplier<Properties> configurationSupplier;

    public InstanceDiscoveryServiceModule(Supplier<Properties> configurationSupplier) {
        this.configurationSupplier = configurationSupplier;
    }

    @Override
    protected void configure() {

        final Properties properties = configurationSupplier.get();
        final String discoveryTypeString = properties.getProperty(INSTANCE_DISCOVERY_SERVICE);

        if (discoveryTypeString == null) {
            addError("Instance discovery type not specified.");
            return;
        }

        final DiscoveryType discoveryType;

        try {
            discoveryType = DiscoveryType.valueOf(discoveryTypeString);
        } catch (IllegalArgumentException ex) {
            addError(ex);
            return;
        }

        install(discoveryType.newModule());
        expose(InstanceDiscoveryService.class);

    }

    public enum DiscoveryType {

        STATIC(StaticInstanceDiscoveryServiceModule::new),

        SPOTIFY_SRV(SpotifySrvInstanceDiscoveryServiceModule::new);

        final Supplier<Module> moduleSupplier;

        DiscoveryType(Supplier<Module> moduleSupplier) {
            this.moduleSupplier = moduleSupplier;
        }

        public Module newModule() {
            return moduleSupplier.get();
        }

    }

}
