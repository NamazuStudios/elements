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
     * @return the a {@link DocumentEntry} instance
     *
     */
    DocumentEntry index(Object model);

    /**
     * Deletes any {@link Document}(s) associated with the given object.  This uses the annotations
     * processed by the {@link #index(Object)} method to find the documents to delete.
     *
     *
     *
     * @param model the model
     */
    void delete(Object model);

    /**
     * Returns a Query for specific types in the search index.  Executing this query
     * will return all instanhes of this type, including subclasses.
     *
     *
     * @param type the type to query for.
     *
     * @return the Query a query generated from this type
     */
    TypeQuery queryForType(final Class<?> type);

    DocumentGenerator getDocumentGenerator();

}
