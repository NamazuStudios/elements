package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;

import javax.print.Doc;

/**
 * Represents a document entry.  This essentially ties and instance of {@link Document} to
 * a particular type as specified by the DocumentT parameter.  The type is often times annotated
 * with the {@link SearchableDocument} annotation, as well as the {@link SearchableIdentity}
 * annotation.
 *
 * Classes implementing this interface are responsible for interpreting fields in the underlying
 * {@link Document}.
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
     * Returns a representation of this with the given super type.
     *
     * @param cls
     * @param <DocumentSuperT>
     * @return
     */
    <DocumentSuperT> DocumentEntry<DocumentSuperT> as(final Class<? super DocumentT> cls);

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
