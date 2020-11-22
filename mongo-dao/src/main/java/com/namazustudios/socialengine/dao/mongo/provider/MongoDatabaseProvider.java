package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoDatabaseProvider implements Provider<MongoDatabase> {

    @Inject
    private Provider<MongoClient> mongoClientProvider;

    @Inject
    @Named(MongoDatastoreProvider.DATABASE_NAME)
    private String databaseName;

    @Override
    public MongoDatabase get() {
        final MongoClient mongoClient = mongoClientProvider.get();
        return mongoClient.getDatabase(databaseName);
    }

}
