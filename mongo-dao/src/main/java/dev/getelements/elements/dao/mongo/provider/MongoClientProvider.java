package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.dao.mongo.codec.TimestampCodec;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;
import static org.bson.codecs.configuration.CodecRegistries.*;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoClientProvider implements Provider<MongoClient> {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);

    private String mongoDbUri;

    private SslSettings sslSettings;

    @Override
    public MongoClient get() {
        return getWithClientUri();
    }

    private MongoClient getWithClientUri() {

        logger.info("Using Connection String {}", getMongoDbUri());

        final var registry = fromRegistries(
            fromCodecs(new TimestampCodec()),
            fromRegistries(
                    getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()))
        );

        final var connectionString = new ConnectionString(getMongoDbUri());

        final var settingsBuilder = MongoClientSettings.builder()
                .codecRegistry(registry)
                .applyConnectionString(connectionString)
                .applyToSslSettings(builder -> builder.applySettings(getSslSettings()));

        return MongoClients.create(settingsBuilder.build());

    }

    public String getMongoDbUri() {
        return mongoDbUri;
    }

    @Inject
    public void setMongoDbUri(@Named(MONGO_CLIENT_URI) String mongoDbUri) {
        this.mongoDbUri = mongoDbUri;
    }

    public SslSettings getSslSettings() {
        return sslSettings;
    }

    @Inject
    public void setSslSettings(SslSettings sslSettings) {
        this.sslSettings = sslSettings;
    }

}
