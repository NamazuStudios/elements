package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.index.IndexPlan;

/**
 * Provides an abstract way to apply indexes to the database.
 */
public interface IndexDao {

    /**
     * Plans all index operations.
     */
    void plan();

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
         * Ends the indexing process.
         */
        @Override
        void close();

    }

}
