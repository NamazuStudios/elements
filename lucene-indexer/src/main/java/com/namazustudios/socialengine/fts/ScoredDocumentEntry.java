package com.namazustudios.socialengine.fts;

/**
 * Returned by some {@link SearchResult} instances to include a score
 * with the result of the search.
 *
 * Created by patricktwohig on 5/16/15.
 */
public interface ScoredDocumentEntry<DocumentT> extends DocumentEntry<DocumentT> {

    /**
     * Gets the score of the document.
     *
     * @return the score
     */
    double getScore();

    /**
     *
     * {@see {@link DocumentEntry#as(Class)}}
     *
     * @param cls
     * @param <DocumentSuperT>
     * @return
     */
    @Override
    <DocumentSuperT> ScoredDocumentEntry<DocumentSuperT> as(final Class<? super DocumentT> cls);

}
