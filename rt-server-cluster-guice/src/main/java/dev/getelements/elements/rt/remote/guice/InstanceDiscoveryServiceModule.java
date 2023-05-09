package dev.getelements.elements.rt.remote.guice;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.remote.InstanceDiscoveryService;
import dev.getelements.elements.rt.remote.JndiSrvInstanceDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.function.Supplier;

import static dev.getelements.elements.rt.Constants.INSTANCE_DISCOVERY_SERVICE;

public class InstanceDiscoveryServiceModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(InstanceDiscoveryServiceModule.class);

    private final Supplier<Properties> configurationSupplier;

    public InstanceDiscoveryServiceModule(final Supplier<Properties> configurationSupplier) {
        this.configurationSupplier = configurationSupplier;
    }

    @Override
    protected void configure() {

        final var properties = configurationSupplier.get();
        final var discoveryTypeString = properties.getProperty(INSTANCE_DISCOVERY_SERVICE);

        if (discoveryTypeString == null) {
            addError("Instance discovery type not specified.");
            return;
        }

        final DiscoveryType discoveryType;

        try {
            logger.info("Using discovery type {}", discoveryTypeString.trim());
            discoveryType = DiscoveryType.valueOf(discoveryTypeString.trim());
        } catch (IllegalArgumentException ex) {
            addError(ex);
            return;
        }

        install(discoveryType.newModule());
        expose(InstanceDiscoveryService.class);

    }

    public enum DiscoveryType {

        STATIC(StaticInstanceDiscoveryServiceModule::new),

        JNDI_SRV(JndiSrvInstanceDiscoveryServiceModule::new),

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
