package dev.getelements.elements.dao.mongo.migration;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.morphia.query.filters.Filters.eq;

/**
 * Applies pending {@link Migration}s, in ascending {@link Migration#getId()} order, tracking completion in the
 * {@code schema_migrations} collection ({@link SchemaMigrationRecord}) so each migration runs at most once.
 * <p>
 * Race-safety mirrors {@code dev.getelements.elements.service.goods.ProductBundleMigration}: a migration is
 * "claimed" by inserting its tracking record before it runs, relying on the {@code _id} uniqueness of
 * {@link SchemaMigrationRecord} to reject a second, concurrent claim rather than any new locking machinery.
 */
public class MigrationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

    private Datastore datastore;

    private Set<Migration> migrations;

    /**
     * Applies every pending migration, in id order. A migration whose claim record already exists is skipped.
     */
    public void run() {
        getMigrationsInOrder().forEach(this::applyIfPending);
    }

    /**
     * The migrations that have not yet been applied, in the order they would be applied.
     *
     * @return the pending migrations
     */
    public List<Migration> getPending() {
        final var appliedIds = getAppliedIds();
        return getMigrationsInOrder().stream()
                .filter(migration -> !appliedIds.contains(migration.getId()))
                .toList();
    }

    /**
     * The ids of every migration that has already been applied.
     *
     * @return the applied migration ids
     */
    public Set<String> getAppliedIds() {
        try (var iterator = getDatastore().find(SchemaMigrationRecord.class).iterator()) {
            return iterator.toList()
                    .stream()
                    .map(SchemaMigrationRecord::getMigrationId)
                    .collect(Collectors.toSet());
        }
    }

    private List<Migration> getMigrationsInOrder() {
        return getMigrations().stream()
                .sorted(Comparator.comparing(Migration::getId))
                .toList();
    }

    private void applyIfPending(final Migration migration) {

        final var record = new SchemaMigrationRecord();
        record.setMigrationId(migration.getId());
        record.setAppliedAt(new Date());

        try {
            getDatastore().insert(record);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() != ErrorCategory.DUPLICATE_KEY) {
                throw e;
            }
            logger.debug("Migration {} already applied, skipping.", migration.getId());
            return;
        }

        logger.info("Applying migration {}: {}", migration.getId(), migration.getDescription());

        try {
            migration.apply(getDatastore());
        } catch (final RuntimeException e) {
            // Release the claim so a later run retries this migration instead of treating a failed
            // attempt as permanently applied.
            getDatastore().find(SchemaMigrationRecord.class).filter(eq("_id", migration.getId())).delete();
            throw e;
        }

        logger.info("Applied migration {}", migration.getId());

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public Set<Migration> getMigrations() {
        return migrations;
    }

    @Inject
    public void setMigrations(Set<Migration> migrations) {
        this.migrations = migrations;
    }

}
