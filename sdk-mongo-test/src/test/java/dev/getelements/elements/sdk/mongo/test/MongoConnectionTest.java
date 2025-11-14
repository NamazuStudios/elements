package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.mongo.MongoConfiguration;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.MongoSslConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

public class MongoConnectionTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionTest.class);

    public static final int TEST_PORT_SSL = 49000;

    public static final int TEST_PORT_PLAIN = 49001;

    private final MongoTestInstance mongoSslTestInstance = new SslCliMongoTestInstance(TEST_PORT_SSL);

    private final MongoTestInstance mongoPlainTestInstance = new DockerMongoTestInstance(TEST_PORT_PLAIN);

    public MongoConnectionTest() {
    }

    @BeforeClass
    public void startSslMongoDB() {
        mongoSslTestInstance.start();
    }

    @AfterClass
    public void stopSslMongoDB() {
        mongoSslTestInstance.stop();
    }

    @BeforeClass
    public void startPlainMongoDB() {
        mongoPlainTestInstance.start();
    }

    @AfterClass
    public void stopPlainMongoDB() {
        mongoPlainTestInstance.stop();
    }

    @DataProvider
    public static Object[][] settings() {
        return new Object[][] {
                new Object[] {  getMongoConfigurationSsl() },
                new Object[] {  getMongoConfigurationPlain() }
        };
    }

    public static MongoConfiguration getMongoConfigurationSsl() {

        final var registry = Guice
                .createInjector(new SslEnabledModule(false, TEST_PORT_SSL))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        return registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

    }

    public static MongoConfiguration getMongoConfigurationPlain() {

        final var registry = Guice
                .createInjector(new SslDisabledModule(false, TEST_PORT_PLAIN))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        return registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

    }

    @Test(dataProvider = "settings")
    public void testConnectPlain(final MongoConfiguration conf) {

        final var connectionString = new ConnectionString(conf.connectionString());

        final var sslSettings = conf
                .findSslConfiguration()
                .map(MongoSslConfiguration::newSslContext)
                .map(sslContext -> SslSettings
                        .builder()
                        .enabled(true)
                        .context(sslContext)
                )
                .orElseGet(() -> SslSettings.builder().applyConnectionString(connectionString))
                .build();

        final var clientSettings = MongoClientSettings
                .builder()
                .applyConnectionString(connectionString)
                .applyToSslSettings(builder -> builder.applySettings(sslSettings))
                .build();

        try (final var client = MongoClients.create(clientSettings)) {
            final var desc = client.getClusterDescription();
            logger.info("Got MongoDB Description: {}", desc.getShortDescription());
        }


    }

}
