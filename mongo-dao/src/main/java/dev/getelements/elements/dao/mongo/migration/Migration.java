package dev.getelements.elements.dao.mongo.migration;

import dev.morphia.Datastore;

/**
 * A single operator-run database migration. Implementations back-fill or reshape existing MongoDB data
 * when a schema-shaping feature ships, and are applied at most once (tracked via {@link SchemaMigrationRecord})
 * by a {@link MigrationRunner}.
 *
 * Implementations must be idempotent and safe to re-run: {@link MigrationRunner} claims a migration's id in the
 * tracking collection before invoking {@link #apply(Datastore)}, but a crash between the claim and completion
 * (or two runners racing against the same database) means {@link #apply(Datastore)} could still observe partially
 * applied state.
 */
public interface Migration {

    /**
     * A globally unique, lexicographically sortable id, conventionally prefixed with a date/sequence stamp
     * (e.g. {@code "20260819_01_backfill_profile_slot_zero"}) so {@link MigrationRunner} can apply migrations
     * in a stable, chronological order.
     *
     * @return the migration id
     */
    String getId();

    /**
     * A short human-readable description of what this migration does, surfaced in logs and CLI status output.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Applies this migration against the given {@link Datastore}.
     *
     * @param datastore the datastore to migrate
     */
    void apply(Datastore datastore);

}
