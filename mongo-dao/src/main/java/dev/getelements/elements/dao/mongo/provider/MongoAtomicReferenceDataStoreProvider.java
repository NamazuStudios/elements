package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import dev.getelements.elements.dao.mongo.MongoElementEntityRegistrar;
import dev.getelements.elements.dao.mongo.migration.PreDatastoreMigrationRunner;
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

    @Inject
    private Provider<PreDatastoreMigrationRunner> preDatastoreMigrationRunnerProvider;

    @Override
    public AtomicReference<Datastore> get() {

        // The boot datastore must map the platform's own DAO model packages even when the MorphiaConfig in
        // play comes from a deployment that names only its own packages: core DAOs decode those types on
        // this shared datastore (legacy rows carry embedded Morphia discriminators, e.g.
        // steps[n]._t: "MongoStep"), and this is the FIRST datastore constructed -- before any element has
        // triggered a registrar rebuild. Mirrors MongoElementEntityRegistrar.createDatastore.

        final var client = mongoClientProvider.get();
        final var config = MongoElementEntityRegistrar.withPlatformModelPackages(morphiaConfigProvider.get());
        preDatastoreMigrationRunnerProvider.get().run(client, config);
        final var datastore =  createDatastore(client, config);
        return new AtomicReference<>(datastore);

    }

}
