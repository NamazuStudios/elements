package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.AbstractModule;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.guice.ConfigurationModule;

import java.util.Properties;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.*;

public class SslEnabledModule extends AbstractModule {

    private final int port;

    private final Boolean insecure;

    public SslEnabledModule() {
        this(null);
    }

    public SslEnabledModule(final Boolean insecure) {
        this(insecure, 27017);
    }

    public SslEnabledModule(final Boolean insecure, final int port) {
        this.port = port;
        this.insecure = insecure;
    }

    @Override
    protected void configure() {

        final var certs = new MongoTestSslCertificates();

        final var uri = insecure == null
                ? "mongodb://localhost:%d/?tls=true".formatted(port)
                : "mongodb://localhost:%d/?tls=true&tlsinsecure=%s".formatted(port, insecure);

        install(new MongoSdkTestElementModule());

        install(new ConfigurationModule(() -> {
            final var properties = new Properties(new DefaultConfigurationSupplier().get());
            properties.put(CA, certs.getCaP12().toAbsolutePath().toString());
            properties.put(CLIENT_CERTIFICATE, certs.getClientP12().toAbsolutePath().toString());
            properties.put(MONGO_CLIENT_URI, uri);
            return properties;
        }));

    }

}
