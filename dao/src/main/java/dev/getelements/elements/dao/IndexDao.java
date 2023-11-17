package dev.getelements.elements.dao;

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
