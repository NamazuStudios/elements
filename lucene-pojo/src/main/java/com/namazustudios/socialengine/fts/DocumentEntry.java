package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 *
 *
 * Created by patricktwohig on 5/16/15.
 */
public interface DocumentEntry<DocumentT> {

    /**
     * Gets the full {@link Document} instance.
     *
     * @return the full {@link Document} instance.
     */
    Document getDocument();

    /**
     * Extracts and generates the {@link Identity} for the encapsulated {@link Document} by reading
     * the annotations on the given class and extracting values from the document.
     *
     * @return the identity for this document
     *
     * @throws DocumentException if there is a problem generating the document's identity
     */

    Identity<DocumentT> getIdentifier(Class<DocumentT> aClass);

}
