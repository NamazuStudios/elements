package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import dev.getelements.elements.dao.mongo.migration.PreDatastoreMigrationRunner;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.concurrent.atomic.AtomicReference;

import static dev.morphia.Morphia.createDatastore;

/**
 * Builds the {@link Datastore} used by the migration tool, applying indexes itself via
 * {@link SelfHealingIndexApplier} rather than relying on {@code MorphiaConfig.applyIndexes(true)}, so a stale
 * on-disk index left over from a prior index-option change is healed here -- as an explicit ops step via the
 * {@code migrate} command -- instead of aborting datastore construction.
 *
 * <p>Deliberately <b>not</b> bound on the server boot path ({@code MongoDaoElementModule}): the server keeps
 * {@code applyIndexes(true)} so boot stays fast and predictable (health checks during scaling do not wait on
 * index builds). Deployments are expected to run {@code migrate} before starting the server; the migrate tool's
 * bootstrap ({@code MongoDatastoreBootstrapModule}) routes through this provider and resolves any conflicts.
 * A boot on an unmigrated database fails fast with the underlying Mongo error rather than stalling.</p>
 */
public class SelfHealingAtomicReferenceDataStoreProvider implements Provider<AtomicReference<Datastore>> {

    @Inject
    private Provider<MongoClient> mongoClientProvider;

    @Inject
    private Provider<MorphiaConfig> morphiaConfigProvider;

    @Inject
    private Provider<PreDatastoreMigrationRunner> preDatastoreMigrationRunnerProvider;

    @Inject
    private SelfHealingIndexApplier selfHealingIndexApplier;

    @Override
    public AtomicReference<Datastore> get() {
        final var client = mongoClientProvider.get();
        final var config = morphiaConfigProvider.get();
        preDatastoreMigrationRunnerProvider.get().run(client, config);
        final var datastore = createDatastore(client, config);
        selfHealingIndexApplier.applyIndexes(datastore);
        return new AtomicReference<>(datastore);
    }

}
