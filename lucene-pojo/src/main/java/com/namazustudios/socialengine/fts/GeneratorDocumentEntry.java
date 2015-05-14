package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Represents a Key in index for a particular document.
 *
 * Created by patricktwohig on 5/13/15.
 */
public class GeneratorDocumentEntry implements DocumentEntry<Object, Object> {

    private final Document document;

    public GeneratorDocumentEntry() {
        this(new Document());
    }
    public GeneratorDocumentEntry(final Document document) {
        this.document = document;
    }

    @Override
    public Document getDocument() {
        return document;
    }

}
