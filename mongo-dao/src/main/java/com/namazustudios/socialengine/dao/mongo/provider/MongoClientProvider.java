package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoClientProvider implements Provider<MongoClient> {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);

    public static final String MONGO_CLIENT_URI = "com.namazustudios.socialengine.mongo.client.uri";

    private String mongoDbUri;

    @Override
    public MongoClient get() {
        return getWithClientUri();
    }

    private MongoClient getWithClientUri() {
        logger.info("Using Connection String.");
        final ConnectionString connectionString = new ConnectionString(getMongoDbUri());
        return MongoClients.create(connectionString);
    }

    public String getMongoDbUri() {
        return mongoDbUri;
    }

    @Inject
    public void setMongoDbUri(@Named(MONGO_CLIENT_URI) String mongoDbUri) {
        this.mongoDbUri = mongoDbUri;
    }

}
