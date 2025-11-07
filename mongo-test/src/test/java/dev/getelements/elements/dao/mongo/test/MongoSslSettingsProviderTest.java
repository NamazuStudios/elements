package dev.getelements.elements.dao.mongo.test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.provider.MongoSslSettingsProvider;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.mongo.test.MongoTestSslCertificates;
import org.testng.annotations.Test;

import java.util.Properties;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.*;
import static java.lang.String.format;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertFalse;


public class MongoSslSettingsProviderTest {


    @Test
    public void testWithSslEnabledSecure() {

        final var sslSettings = Guice
                .createInjector(new SslEnabledModule(false))
                .getInstance(SslSettings.class);

        assertTrue(sslSettings.isEnabled());
        assertFalse(sslSettings.isInvalidHostNameAllowed());
        assertNotNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslEnabledInsecure() {

        final var sslSettings = Guice
                .createInjector(new SslEnabledModule(true))
                .getInstance(SslSettings.class);

        assertTrue(sslSettings.isEnabled());
        assertTrue(sslSettings.isInvalidHostNameAllowed());
        assertNotNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslEnabledDefault() {

        final var sslSettings = Guice
                .createInjector(new SslEnabledModule())
                .getInstance(SslSettings.class);

        assertTrue(sslSettings.isEnabled());
        assertFalse(sslSettings.isInvalidHostNameAllowed());
        assertNotNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslDisabledExplicit() {

        final var sslSettings = Guice
                .createInjector(new SslDisabledModule(true))
                .getInstance(SslSettings.class);

        assertFalse(sslSettings.isEnabled());
        assertNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslDisabledImplicit() {

        final var sslSettings = Guice
                .createInjector(new SslDisabledModule(false))
                .getInstance(SslSettings.class);

        assertFalse(sslSettings.isEnabled());
        assertNull(sslSettings.getContext());

    }

    public static class SslEnabledModule extends AbstractModule {

        private final Boolean insecure;

        public SslEnabledModule() {
            this.insecure = null;
        }

        public SslEnabledModule(final Boolean insecure) {
            this.insecure = insecure;
        }

        @Override
        protected void configure() {

            final var certs = new MongoTestSslCertificates();

            final var uri = insecure == null
                    ? "mongodb://localhost/?tls=true"
                    : format("mongodb://localhost/?tls=true&tlsinsecure=%s", insecure);

            install(new ConfigurationModule(() -> {
                final var properties = new Properties(new DefaultConfigurationSupplier().get());
                properties.put(CA, certs.getCaP12().toAbsolutePath().toString());
                properties.put(CLIENT_CERTIFICATE, certs.getClientP12().toAbsolutePath().toString());
                properties.put(MONGO_CLIENT_URI, uri);
                return properties;
            }));

            bind(SslSettings.class).toProvider(MongoSslSettingsProvider.class);

        }

    }

    public static class SslDisabledModule extends AbstractModule {

        private final boolean explicit;

        public SslDisabledModule(final boolean explicit) {
            this.explicit = explicit;
        }

        @Override
        protected void configure() {

            final var uri = explicit
                    ? "mongodb://localhost/?tls=true"
                    : "mongodb://localhost";

            install(new ConfigurationModule(() -> {
                final var properties = new Properties(new DefaultConfigurationSupplier().get());
                properties.put(MONGO_CLIENT_URI, uri);
                return properties;
            }));

            bind(SslSettings.class).toProvider(MongoSslSettingsProvider.class);

        }

    }

}
