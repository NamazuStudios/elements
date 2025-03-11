package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.index.IndexPlan;
import dev.getelements.elements.sdk.model.index.IndexableType;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Provides an abstract way to apply indexes to the database.
 */
@ElementServiceExport
public interface IndexDao {

    /**
     * Plans all index operations.
     */
    void planAll();

    /**
     * Plans a specific type.
     *
     * @param type the type
     */
    void planType(IndexableType type);

    /**
     * Starts the index process.
     *
     * @return the {@link Indexer}
     */
    Indexer beginIndexing();

    /**
     * Gets all plans in the system.
     *
     * @param offset
     * @param count
     * @return
     */
    Pagination<IndexPlan<?>> getPlans(int offset, int count);

    /**
     * Executes an index operation within a scope which controls multiple concurrent accesses
     */
    interface Indexer extends AutoCloseable {

        /**
         * Builds all custom indexes (eg metadata)
         */
        void buildAllCustom();

        /**
         * Builds for a specific indexable type.
         *
         * @param indexableType the indexable type
         */
        void buildCustomIndexesFor(IndexableType indexableType);

        /**
         * Ends the indexing process.
         */
        @Override
        void close();

    }

}
