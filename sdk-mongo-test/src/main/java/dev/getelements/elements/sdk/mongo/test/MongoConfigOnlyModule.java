package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.AbstractModule;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.StandardMongoConfigurationService;
import dev.getelements.elements.sdk.mongo.provider.MongoSslSettingsProvider;

import java.util.Properties;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.*;

/**
 * Slim test module that wires {@link MongoConfigurationService} and {@link SslSettings} from a fixed connection
 * string without installing {@code MongoSdkTestElementModule} (and therefore without binding {@code MongoClient}).
 *
 * <p>Suitable for unit-style tests that only exercise the SSL settings / configuration resolution code paths.
 * Tests that need a live driver should instead use {@link SslEnabledModule} / {@link SslDisabledModule} with
 * an explicit port and a corresponding {@link MongoTestInstance}.</p>
 */
public class MongoConfigOnlyModule extends AbstractModule {

    /** SSL modes for {@link #MongoConfigOnlyModule(Mode, Boolean)}. */
    public enum Mode {
        /** {@code mongodb://localhost/?tls=true} — TLS on, certificates wired from test-bundled keystores. */
        SSL_ENABLED,
        /** {@code mongodb://localhost/?tls=false} — TLS explicitly disabled in the URI. */
        SSL_DISABLED_EXPLICIT,
        /** {@code mongodb://localhost} — TLS flag absent from the URI. */
        SSL_DISABLED_IMPLICIT
    }

    private final Mode mode;

    private final Boolean insecure;

    public MongoConfigOnlyModule(final Mode mode) {
        this(mode, null);
    }

    /**
     * @param mode SSL URI mode
     * @param insecure when {@code mode == SSL_ENABLED}, whether to set {@code tlsinsecure} in the URI;
     *                 ignored otherwise. {@code null} omits the flag.
     */
    public MongoConfigOnlyModule(final Mode mode, final Boolean insecure) {
        this.mode = mode;
        this.insecure = insecure;
    }

    @Override
    protected void configure() {

        final var uri = switch (mode) {
            case SSL_ENABLED -> insecure == null
                    ? "mongodb://localhost/?tls=true"
                    : "mongodb://localhost/?tls=true&tlsinsecure=" + insecure;
            case SSL_DISABLED_EXPLICIT -> "mongodb://localhost/?tls=false";
            case SSL_DISABLED_IMPLICIT -> "mongodb://localhost";
        };

        install(new ConfigurationModule(() -> {
            final var properties = new Properties(new DefaultConfigurationSupplier().get());
            properties.put(MONGO_CLIENT_URI, uri);

            if (mode == Mode.SSL_ENABLED) {
                final var certs = new MongoTestSslCertificates();
                properties.put(CA, certs.getCaP12().toAbsolutePath().toString());
                properties.put(CLIENT_CERTIFICATE, certs.getClientP12().toAbsolutePath().toString());
            }

            return properties;
        }));

        bind(MongoConfigurationService.class).to(StandardMongoConfigurationService.class);
        bind(SslSettings.class).toProvider(MongoSslSettingsProvider.class);
    }
}