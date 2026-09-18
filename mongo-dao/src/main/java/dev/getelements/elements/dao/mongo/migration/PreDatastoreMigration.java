package dev.getelements.elements.dao.mongo.migration;

import com.mongodb.client.MongoDatabase;

/**
 * A migration that must run before the Morphia {@link dev.morphia.Datastore} is constructed — e.g. raw
 * index cleanup needed to avoid a conflict that would otherwise crash {@code Morphia.createDatastore(...)}
 * itself (see {@code dev.getelements.elements.dao.mongo.migration.migrations.DropLegacyAuthSchemeIndexesMigration}
 * for a concrete example).
 *
 * <p>Because no {@link dev.morphia.Datastore} exists yet when these run, implementations operate directly
 * against the raw driver via {@link MongoDatabase} rather than Morphia, and are tracked (via
 * {@link PreDatastoreMigrationRunner}) in a plain, non-Morphia-mapped collection rather than
 * {@link SchemaMigrationRecord}.
 *
 * <p>Implementations must be idempotent and safe to re-run, same as {@link Migration}: a crash between
 * this migration applying and its completion being recorded means {@link #apply(MongoDatabase)} could
 * run again.
 */
public interface PreDatastoreMigration {

    /**
     * A globally unique, lexicographically sortable id, conventionally prefixed with a date/sequence stamp
     * (e.g. {@code "20260821_01_drop_legacy_auth_scheme_indexes"}) so {@link PreDatastoreMigrationRunner}
     * can apply migrations in a stable, chronological order.
     *
     * @return the migration id
     */
    String getId();

    /**
     * A short human-readable description of what this migration does, surfaced in logs.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Applies this migration against the given raw {@link MongoDatabase}.
     *
     * @param database the database to migrate
     */
    void apply(MongoDatabase database);

}
