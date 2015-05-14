package com.namazustudios.socialengine.fts;

import org.apache.lucene.search.Query;

/**
 * Created by patricktwohig on 5/13/15.
 */
public interface Index {

    /**
     * Adds an object to the index.  The object must be a type which has the @
     *
     * @param model
     */
    void add(Object model);

    /**
     * Deletes the object from the index.
     *
     * @param model
     */
    void delete(Object model);

//    /**
//     * Returns an {@link Iterable} of {@link GeneratorDocumentEntry} objects using the given
//     * query parameters.
//     *
//     * @param cls the Class to search for
//     * @param lql the Lucene query string
//     * @param <IdentifierT> the identifier type
//     * @param <ClassT>
//     *
//     * @return the Itrable of documents
//     */
//    <IdentifierT, ClassT> Iterable<GeneratorDocumentEntry<IdentifierT, ClassT>>
//        search(final Class<IdentifierT> identifierClass, final Class<ClassT> cls, final String lql);
//
//    /**
//     *
//     * Returns an {@link Iterable} of {@link GeneratorDocumentEntry} objects using the given
//     * query parameters.
//     *
//     * @param cls the Class to search for
//     * @param lql the Lucene query string
//     * @param query a {@link Query} which will be ANDed to the given LQL
//     * @param <IdentifierT> the identifier type
//     * @param <ClassT>
//     *
//     * @return the Iterable of documents
//     */
//    <IdentifierT, ClassT> Iterable<GeneratorDocumentEntry<IdentifierT, ClassT>>
//        search(final Class<IdentifierT> identifierClass, final Class<ClassT> cls, final String lql, final Query query);

}
