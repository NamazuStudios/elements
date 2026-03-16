package dev.getelements.elements.sdk.mongo.provider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoDatabaseProvider implements Provider<MongoDatabase> {

    @Inject
    private Provider<MongoClient> mongoClientProvider;

    @Inject
    @Named(MongoConfigurationService.DATABASE_NAME)
    private String databaseName;

    @Override
    public MongoDatabase get() {
        final var client = mongoClientProvider.get();
        return client.getDatabase(databaseName);
    }

}
