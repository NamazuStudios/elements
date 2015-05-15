package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Created by patricktwohig on 5/14/15.
 */
public interface DocumentEntry<IdentifierT, DocumentT> {
    
    /**
     * Gets the full {@link Document} instance.
     *
     * @return the full {@link Document} instance.
     */
    Document getDocument();

}
