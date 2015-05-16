package com.namazustudios.socialengine.fts;

/**
 * This is the final result from a query.  This provides a means
 * to walk the collection of documents requested through an {@link ObjectQuery}.
 *
 * Created by patricktwohig on 5/15/15.
 */
public interface SearchResult<DocumentT> extends Iterable<DocumentEntry<DocumentT>> {

    /**
     * Gets the total number of results in the set.  This may be an
     * estimate or an approximation of the actual number of documents
     * the query will return.
     *
     * @return the total number of results
     */
    int total();

}
