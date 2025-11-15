package dev.getelements.elements.dao.mongo.test;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;

public class SimpleMongoProvider implements Provider<MongoClient> {

    private String mongoDbUri;

    @Override
    public MongoClient get() {
        final var connectionString = new ConnectionString(getMongoDbUri());

        final var settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(settings);

    }

    public String getMongoDbUri() {
        return mongoDbUri;
    }

    @Inject
    public void setMongoDbUri(@Named("dev.getelements.elements.mongo.uri") String mongoDbUri) {
        this.mongoDbUri = mongoDbUri;
    }

}
