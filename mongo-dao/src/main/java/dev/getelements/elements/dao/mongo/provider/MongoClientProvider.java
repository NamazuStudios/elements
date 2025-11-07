package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.dao.mongo.codec.TimestampCodec;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.MongoSslConfiguration;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.Optional;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.*;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoClientProvider implements Provider<MongoClient> {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);

    private ElementRegistry registry;

    @Override
    public MongoClient get() {
        return getWithClientUri();
    }

    private MongoClient getWithClientUri() {

        final var configuration = getRegistry()
                .find("dev.getlements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        logger.info("Using Connection String {}", configuration.connectionString());

        final var registry = fromRegistries(
            fromCodecs(new TimestampCodec()),
            fromRegistries(
                    getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()))
        );

        final var connectionString = new ConnectionString(configuration.connectionString());

        final var sslSettings = configuration.findSessionConfiguration()
                .map(MongoSslConfiguration::newSslContext)
                .map(sslContext -> SslSettings.builder()
                        .enabled(true)
                        .context(sslContext)
                        .applyConnectionString(connectionString)
                        .build())
                .orElseGet(() -> SslSettings.builder().enabled(false).build());

        final var settingsBuilder = MongoClientSettings.builder()
                .codecRegistry(registry)
                .applyConnectionString(connectionString)
                .applyToSslSettings(builder -> builder.applySettings(sslSettings));

        return MongoClients.create(settingsBuilder.build());

    }

    public ElementRegistry getRegistry() {
        return registry;
    }

    @Inject
    public void setRegistry(ElementRegistry registry) {
        this.registry = registry;
    }

}
