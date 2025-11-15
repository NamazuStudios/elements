package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.AbstractModule;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.guice.ConfigurationModule;

import java.util.Properties;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;

public class SslDisabledModule extends AbstractModule {

    private final int port;

    private final boolean explicit;

    public SslDisabledModule(final boolean explicit) {
        this(explicit, 27017);
    }

    public SslDisabledModule(final boolean explicit, final int port) {
        this.port = port;
        this.explicit = explicit;
    }

    @Override
    protected void configure() {

        final var uri = explicit
                ? "mongodb://localhost:%d/?tls=false".formatted(port)
                : "mongodb://localhost:%d".formatted(port);

        install(new MongoSdkTestElementModule());

        install(new ConfigurationModule(() -> {
            final var properties = new Properties(new DefaultConfigurationSupplier().get());
            properties.put(MONGO_CLIENT_URI, uri);
            return properties;
        }));

    }

}
