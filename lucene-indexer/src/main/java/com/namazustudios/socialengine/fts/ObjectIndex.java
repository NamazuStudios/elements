package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

/**
 * Manages objects in a Lucene search index.
 *
 * Objects must be a type which has the {@link SearchableDocument}
 * annotation for any meaningful fields to be added to the index.
 *
 * It is also strongly recommended that the the {@link SearchableIdentity}
 * be present somewhere in the class hierarchy for the object so that the objects
 * can be recalled later.
 *
 * Created by patricktwohig on 5/13/15.
 */
public interface ObjectIndex {

    /**
     *
     * Adds an object to the index, or updates an existing object's existing {@link Document}s
     *
     * Before adding new {@link Document}s, any existing records are first deleted.  This is similar
     * to how {@link IndexWriter#updateDocument(Term, Iterable)} behaves.
     *
     * Upon completion, this {@link IndexWriter#commit()}s the changes.
     *
     * @param model the object
     * @return the a {@link BasicDocumentEntry} instance
     *
     */
    <T> DocumentEntry<T> index(Class<T> type, T model);

    /**
     * Deletes any {@link Document}(s) associated with the given object.  This uses the annotations
     * processed by the {@link #index(Class, Object)} method to find the documents to delete.
     *
     *
     *
     * @param model the model
     */
    <T> void delete(Class<T> type, T model);

    /**
     * Returns an @{link ObjectQuery} for specific types in the search index.  Executing this query
     * will return all instances of this type, including subclasses.
     *
     * @param type the type for which to query
     * @return a query generated from this type
     *
     */
    <T> ObjectQuery<T> queryForType(final Class<T> type);

    /**
     * Returns an @{link ObjectQuery} for a specific type and identifier in the search index.  Excuting
     * this query qill return a single instance identified with the type.
     *
     * @param type the type for which to query
     * @param identifier the identifier of the object
     * @return a query generated from this type
     */
    <T> ObjectQuery<T> queryForIdentifier(final Class<T> type, Object identifier);

    /**
     * Returns an @{link ObjectQuery} for a specific type and identifier in the search index.  Excuting
     * this query qill return a single instance identified with the type.
     *
     * @param type the type for which to query
     * @param query Lucene {@link Query} to query for objects
     * @return a query generated from this type
     */
    <T> ObjectQuery<T> queryForObjects(final Class<T> type, Query query);

    /**
     * Creates a query that will return the {@link Document} for the given object.
     *
     * @param type the type
     * @param object
     * @return
     */
    <T> ObjectQuery<T> queryByExample(final  Class<T> type, T object);

    /**
     * Performs the given Query and returns the {@link QueryExecutor} object.
     *
     * @param query
     * @param <T>
     * @return
     */
    <T> QueryExecutor<T> execute(ObjectQuery<T> query);

    /**
     * Creates and executes a query for the given type.
     *
     * {@see {@link #queryForType(Class)}}
     * {@see {@link #execute(ObjectQuery)}}
     *
     * @param type
     */
    <T> QueryExecutor<T> executeQueryForType(final Class<T> type);

    /**
     * Creates and executes a query for the given type and identifier.
     *
     * {@see {@link #queryForIdentifier(Class, Object)}}
     * {@see {@link #execute(ObjectQuery)}}
     *
     * @param type
     */
    <T> QueryExecutor<T> executeQueryForIdentifier(final Class<T> type, Object identifier);

    /**
     * Creates and executes a query for the given type and object query
     *
     * {@see {@link #queryForObjects(Class, Query)}}
     * {@see {@link #execute(ObjectQuery)}}
     *
     * @param type
     */
    <T> QueryExecutor<T> executeQueryForObjects(final Class<T> type, Query query);

    /**
     * Creates and executes a query for the given type and example object
     *
     * {@see {@link #queryByExample(Class, Object)}
     * {@see {@link #execute(ObjectQuery)}}
     *
     * @param type
     */
    <T> QueryExecutor<T> executeQueryByExample(final  Class<T> type, T object);

}
