package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import org.testng.annotations.Test;

import java.util.Properties;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class MongoSslSettingsProviderTest {

    @Test
    public void testWithSslEnabledSecure() {

        final var registry = Guice
                .createInjector(new SslEnabledModule(false))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());;
        assertFalse(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslEnabledInsecure() {

        final var registry = Guice
                .createInjector(new SslEnabledModule(true))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());
        assertTrue(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslEnabledDefault() {

        final var registry = Guice
                .createInjector(new SslEnabledModule())
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());
        assertFalse(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslDisabledExplicit() {

        final var registry = Guice
                .createInjector(new SslDisabledModule(true))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isEmpty());

    }

    @Test
    public void testWithSslDisabledImplicit() {

        final var registry = Guice
                .createInjector(new SslDisabledModule(false))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isEmpty());

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
                    : "mongodb://localhost/?tls=true&tlsinsecure=%s".formatted(insecure);

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

            install(new MongoSdkTestElementModule());

            install(new ConfigurationModule(() -> {
                final var properties = new Properties(new DefaultConfigurationSupplier().get());
                properties.put(MONGO_CLIENT_URI, uri);
                return properties;
            }));

        }

    }

}
