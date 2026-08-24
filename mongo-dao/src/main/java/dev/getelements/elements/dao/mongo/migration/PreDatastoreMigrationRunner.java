package dev.getelements.elements.dao.mongo.migration;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.morphia.config.MorphiaConfig;
import jakarta.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Applies pending {@link PreDatastoreMigration}s, in ascending {@link PreDatastoreMigration#getId()} order,
 * before the Morphia {@code Datastore} is constructed. Call this immediately before every
 * {@code Morphia.createDatastore(...)} call site sharing the same {@link MorphiaConfig}.
 *
 * <p>Tracks completion in the raw {@code pre_datastore_migrations} collection (not Morphia-mapped, since no
 * {@code Datastore} exists yet) so each migration runs at most once, mirroring {@link MigrationRunner}'s
 * claim-based approach but against the raw driver.
 *
 * <p>A migration that throws is logged and skipped rather than propagated, so a single misbehaving
 * pre-datastore migration can never prevent the server from starting — the exact situation these
 * migrations exist to avoid in the first place.
 */
public class PreDatastoreMigrationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PreDatastoreMigrationRunner.class);

    private static final String COLLECTION = "pre_datastore_migrations";

    private Set<PreDatastoreMigration> migrations;

    /**
     * Applies every pending migration, in id order, against the database resolved from {@code config}.
     *
     * @param client the {@link MongoClient} to use
     * @param config the {@link MorphiaConfig}, used only to resolve the target database name
     */
    public void run(final MongoClient client, final MorphiaConfig config) {

        final var database = client.getDatabase(config.database());
        final var tracking = database.getCollection(COLLECTION);
        final var appliedIds = getAppliedIds(tracking);

        getMigrationsInOrder().forEach(migration -> {
            if (appliedIds.contains(migration.getId())) {
                logger.debug("Pre-datastore migration {} already applied, skipping.", migration.getId());
            } else {
                applyAndClaim(migration, database, tracking);
            }
        });

    }

    private void applyAndClaim(final PreDatastoreMigration migration,
                                final MongoDatabase database,
                                final MongoCollection<Document> tracking) {
        try {

            logger.info("Applying pre-datastore migration {}: {}", migration.getId(), migration.getDescription());
            migration.apply(database);

            tracking.insertOne(new Document("_id", migration.getId()).append("appliedAt", new Date()));
            logger.info("Applied pre-datastore migration {}", migration.getId());

        } catch (final MongoWriteException ex) {
            if (ex.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                logger.debug("Pre-datastore migration {} claimed concurrently, skipping.", migration.getId());
            } else {
                logger.warn("Failed to record completion of pre-datastore migration {}: {}",
                        migration.getId(), ex.getMessage(), ex);
            }
        } catch (final RuntimeException ex) {
            logger.warn("Pre-datastore migration {} failed; will retry on next startup: {}",
                    migration.getId(), ex.getMessage(), ex);
        }
    }

    private Set<String> getAppliedIds(final MongoCollection<Document> tracking) {
        final var ids = new HashSet<String>();
        try (var cursor = tracking.find().iterator()) {
            while (cursor.hasNext()) {
                ids.add(cursor.next().getString("_id"));
            }
        } catch (final RuntimeException ex) {
            // Collection may not exist yet on a fresh install; treat as "nothing applied."
            logger.debug("Could not read pre-datastore migration tracking collection: {}", ex.getMessage());
        }
        return ids;
    }

    private List<PreDatastoreMigration> getMigrationsInOrder() {
        return getMigrations().stream()
                .sorted(Comparator.comparing(PreDatastoreMigration::getId))
                .toList();
    }

    public Set<PreDatastoreMigration> getMigrations() {
        return migrations;
    }

    @Inject
    public void setMigrations(final Set<PreDatastoreMigration> migrations) {
        this.migrations = migrations;
    }

}
