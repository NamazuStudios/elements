package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.namazustudios.socialengine.dao.mongo.codec.TimestampCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoClientProvider implements Provider<MongoClient> {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);

    public static final String MONGO_CLIENT_URI = "com.namazustudios.socialengine.mongo.uri";

    private String mongoDbUri;

    @Override
    public MongoClient get() {
        return getWithClientUri();
    }

    private MongoClient getWithClientUri() {

        logger.info("Using Connection String {}", getMongoDbUri());

        final var registry = fromRegistries(
            fromCodecs(new TimestampCodec()),
            fromRegistries(getDefaultCodecRegistry())
        );

        final var settings = MongoClientSettings.builder()
                .codecRegistry(registry)
                .applyConnectionString(new ConnectionString(getMongoDbUri()))
            .build();

        return MongoClients.create(settings);

    }

    public String getMongoDbUri() {
        return mongoDbUri;
    }

    @Inject
    public void setMongoDbUri(@Named(MONGO_CLIENT_URI) String mongoDbUri) {
        this.mongoDbUri = mongoDbUri;
    }

}
