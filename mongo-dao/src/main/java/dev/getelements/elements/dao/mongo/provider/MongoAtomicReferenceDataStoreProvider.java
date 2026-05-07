package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.concurrent.atomic.AtomicReference;

import static dev.morphia.Morphia.createDatastore;

public class MongoAtomicReferenceDataStoreProvider implements Provider<AtomicReference<Datastore>> {

    @Inject
    private Provider<MongoClient> mongoClientProvider;

    @Inject
    private Provider<MorphiaConfig> morphiaConfigProvider;

    @Override
    public AtomicReference<Datastore> get() {
        final var client = mongoClientProvider.get();
        final var config = morphiaConfigProvider.get();
        final var datastore =  createDatastore(client, config);
        return new AtomicReference<>(datastore);
    }

}
