package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

import javax.print.Doc;

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
     * Extracts and generates the {@link BasicIdentity} for the encapsulated {@link Document} by reading
     * the annotations on the given class and extracting values from the document.
     *
     * @return the identity for this document
     *
     * @throws DocumentException if there is a problem generating the document's identity
     */

    Identity<DocumentT> getIdentity(Class<DocumentT> documentTClassType);

    /**
     * Gets the {@link Fields} of the document.
     *
     * @param documentTClassType the document type
     * @return the document type
     */
    Fields<DocumentT> getFields(Class<DocumentT> documentTClassType);

}
