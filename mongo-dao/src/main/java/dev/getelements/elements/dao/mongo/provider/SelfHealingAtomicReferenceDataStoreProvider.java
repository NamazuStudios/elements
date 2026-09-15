package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.concurrent.atomic.AtomicReference;

import static dev.morphia.Morphia.createDatastore;

/**
 * Builds the {@link Datastore} used by the migration tool, applying indexes itself via
 * {@link SelfHealingIndexApplier} rather than relying on {@code MorphiaConfig.applyIndexes(true)}, so a
 * stale on-disk index left over from a prior index-option change doesn't abort datastore construction.
 */
public class SelfHealingAtomicReferenceDataStoreProvider implements Provider<AtomicReference<Datastore>> {

    @Inject
    private Provider<MongoClient> mongoClientProvider;

    @Inject
    private Provider<MorphiaConfig> morphiaConfigProvider;

    @Inject
    private SelfHealingIndexApplier selfHealingIndexApplier;

    @Override
    public AtomicReference<Datastore> get() {
        final var client = mongoClientProvider.get();
        final var config = morphiaConfigProvider.get();
        final var datastore = createDatastore(client, config);
        selfHealingIndexApplier.applyIndexes(datastore);
        return new AtomicReference<>(datastore);
    }

}
