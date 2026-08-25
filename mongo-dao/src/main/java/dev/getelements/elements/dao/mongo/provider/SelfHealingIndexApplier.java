package dev.getelements.elements.dao.mongo.provider;

import dev.morphia.Datastore;

/**
 * Applies every mapped entity's declared indexes against a {@link Datastore}, healing (via
 * {@link IndexConflictResolver}) any single collection whose on-disk index conflicts with its newly-declared
 * options, instead of letting one conflicting index abort index application for every other entity, which is
 * how {@link Datastore}'s own automatic index application behaves. Callers must configure the datastore's
 * {@code MorphiaConfig.applyIndexes()} to {@code false} so this is the only path that creates indexes.
 */
public interface SelfHealingIndexApplier {

    /**
     * Applies indexes for every entity mapped by {@code datastore}, healing conflicts as they're encountered.
     *
     * @param datastore the datastore whose mapped entities' indexes should be applied
     */
    void applyIndexes(Datastore datastore);

}
