package com.namazustudios.socialengine.fts;

import java.io.IOException;

/**
 * This is the final result from a query.  This provides a means to walk the collection
 * of documents requested through an {@link ObjectQuery}.  Additionally, this is capable
 * of returning the total number of documents matching the query.
 *
 * This extends the {@link AutoCloseable} interface such that it may be used in the
 * try-with-resources block.
 *
 * Created by patricktwohig on 5/15/15.
 */
public interface SearchResult<DocumentT, EntryT extends DocumentEntry<DocumentT>>
        extends Iterable<EntryT>, AutoCloseable {

    /**
     * Gets the total number of results in the set.  This may be an
     * estimate or an approximation of the actual number of documents
     * the query will return.
     *
     * This is not the {@link #available()} number of results, but rather
     * the total number of documents matching the query for the whole index.
     *
     * <em>Caveat: </em>Logic should not rely on the value of this method, as it can
     * be an estimate of total number.
     *
     * @return the total number of results
     */
    int total();

    /**
     * Returns the number of results available to this result.  This is not
     * the {@link #total()} number of results, but the number available
     * from the current result.
     *
     * @return the number of results availble
     */
    int available();

    /**
     * Gets a single result if only interested in a single result.  This will
     * never return null.  If there is more than one result or if there are
     * no results this will throw an exception.  This will never return null.
     *
     * @return the document, not null
     *
     * @throws NoResultException if there are no rusults
     * @throws MultipleResultException if there is more than one result
     *
     */
    DocumentEntry<DocumentT> singleResult();

    /**
     * Prunes this search result to the given count.  This will return a new
     * instance whose result set has been pruned to no greater than the given
     * count.
     *
     * The number of re
     *
     * @param count
     * @return
     */
    SearchResult<DocumentT, EntryT> prune(int count);

    /**
     * Closes this result's underlying {@link IOContext}.
     *
     * @throws SearchException if a problem occurs while reading the
     */
    @Override
    void close();

}
