package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Created by patricktwohig on 5/14/15.
 */
public interface DocumentEntry<IdentifierT, DocumentT> {

    /**
     * Gets the class name for the object.
     *
     * {@link #getDocumentClass()}.equals(getClassName()) must always be true.
     *
     * @return the class name
     */
    String getClassName();

    /**
     * Gets the Class associated with this key.
     *
     * {@link #getClassName()}.equals(getDocumentClass()) must always be true.
     *
     * @return the document's class
     */
    Class<?> getDocumentClass();

    /**
     * Gets the full {@link Document} instance.
     *
     * @return the full {@link Document} instance.
     */
    Document getDocument();

}
