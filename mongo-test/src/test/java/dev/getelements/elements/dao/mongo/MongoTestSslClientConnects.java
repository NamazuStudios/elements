package dev.getelements.elements.dao.mongo;

import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.provider.MongoClientProvider;
import dev.getelements.elements.dao.mongo.provider.MongoSslSettingsProvider;
import dev.getelements.elements.guice.ConfigurationModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Properties;

import static dev.getelements.elements.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static dev.getelements.elements.dao.mongo.provider.MongoSslSettingsProvider.CA;
import static dev.getelements.elements.dao.mongo.provider.MongoSslSettingsProvider.CLIENT_CERTIFICATE;

@Guice(modules = MongoTestSslClientConnects.Module.class)
public class MongoTestSslClientConnects {

    private static final int MONGO_PORT = 48000;

    private static final String MONGO_TEST_VERSION = "6.0.9";

    @Test
    public void doTest() {

    }

    public static class Module extends AbstractModule {


        @Override
        protected void configure() {

            final var certs = new MongoTestSslCertificates();
            final var instance = new SslCliMongoTestInstance(MONGO_PORT, MONGO_TEST_VERSION);
            instance.start();

            install(new ConfigurationModule(() -> {
                final var properties = new Properties(new DefaultConfigurationSupplier().get());
                properties.put(CA, certs.getCaP12().toAbsolutePath().toString());
                properties.put(CLIENT_CERTIFICATE, certs.getClientP12().toAbsolutePath().toString());
                properties.put(MONGO_CLIENT_URI, "mongodb://localhost/?tls=true");
                return properties;
            }));

            binder().bind(MongoClient.class).toProvider(MongoClientProvider.class);
            bind(SslSettings.class).toProvider(MongoSslSettingsProvider.class);

        }

    }

}
