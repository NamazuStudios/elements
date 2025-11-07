package dev.getelements.elements.dao.mongo.test;

import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.provider.MongoClientProvider;
import dev.getelements.elements.dao.mongo.provider.MongoSslSettingsProvider;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.mongo.test.MongoTestSslCertificates;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.Properties;

import static dev.getelements.elements.dao.mongo.test.MongoTestInstance.ELEMENTS_TESTED_VERSION;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.*;
import static java.lang.String.format;
import static org.testng.AssertJUnit.assertNotNull;

@Guice(modules = MongoTestSslClientConnects.Module.class)
public class MongoTestSslClientConnects {

    private static final int MONGO_PORT = 48000;

    private static final String MONGO_TEST_VERSION = ELEMENTS_TESTED_VERSION;

    private MongoClient mongoClient;

    @Test
    public void doTest() {
        final var description = getMongoClient().getClusterDescription();
        assertNotNull(description);
        assertNotNull(getMongoClient().listDatabaseNames());
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @Inject
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public static class Module extends AbstractModule {


        @Override
        protected void configure() {

            final var certs = new MongoTestSslCertificates();
            final var instance = new SslCliMongoTestInstance(MONGO_PORT, MONGO_TEST_VERSION);
            instance.start();

            final var uri = format("mongodb://localhost:%d/?tls=true", MONGO_PORT);

            install(new ConfigurationModule(() -> {
                final var properties = new Properties(new DefaultConfigurationSupplier().get());
                properties.put(CA, certs.getCaP12().toAbsolutePath().toString());
                properties.put(CLIENT_CERTIFICATE, certs.getClientP12().toAbsolutePath().toString());
                properties.put(MONGO_CLIENT_URI, uri);
                return properties;
            }));

            bind(MongoClient.class).toProvider(MongoClientProvider.class);
            bind(SslSettings.class).toProvider(MongoSslSettingsProvider.class);

        }

    }

}
