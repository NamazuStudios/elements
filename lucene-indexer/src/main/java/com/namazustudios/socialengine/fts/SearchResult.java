package com.namazustudios.socialengine.fts;

/**
 * This is the final result from a query.  This provides a means to walk the collection
 * of documents requested through an {@link ObjectQuery}.  Additionally, this is capable
 * of returning the total number of documents matching the query.
 *
 * Created by patricktwohig on 5/15/15.
 */
public interface SearchResult<DocumentT, EntryT extends DocumentEntry<DocumentT>> extends Iterable<EntryT> {

    /**
     * Gets the total number of results in the set.  This may be an
     * estimate or an approximation of the actual number of documents
     * the query will return.
     *
     * <em>Caveat: </em>Logic should not rely on the value of this method, as it can
     * be an estimate of total number.
     *
     * @return the total number of results
     */
    int total();

    /**
     * Gets a single result if only interested in a single result.  This will
     * never return null.  If there is not a single result, this will throw
     * an exception.
     *
     * @return the document, not null
     *
     * @throws NoResultException if there are no rusults
     * @throws MultipleResultException if there is more than one result
     *
     */
    DocumentEntry<DocumentT> singleResult();

}
