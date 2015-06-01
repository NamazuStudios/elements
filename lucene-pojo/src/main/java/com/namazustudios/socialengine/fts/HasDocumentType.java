package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.apache.lucene.document.Document;

/**
 * An interface through which it is possible to obtain a {@link Document}'s Java
 * type.
 *
 * Created by patricktwohig on 5/31/15.
 */
public interface HasDocumentType<DocumentT> {

    /**
     * Gets the Document's type.  This corresponds to {@link SearchableDocument#type()}.  Note,
     * as subclasses of a document type may be stored, this may not be equal to the
     * corresponding type.
     *
     * @return the document's type.
     */
    Class<? extends DocumentT> getDocumentType();

    /**
     * Gets the {@link SearchableDocument} annotation which is associated with this
     * object.
     *
     * @return the {@link SearchableDocument} annotation
     */
    SearchableDocument getSearchableDocument();

}
