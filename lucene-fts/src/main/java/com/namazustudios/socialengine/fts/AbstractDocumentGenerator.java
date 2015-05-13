package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Created by patricktwohig on 5/12/15.
 */
public abstract class AbstractDocumentGenerator implements DocumentGenerator {

    @Override
    public Document generate(Object object) {
        final Document document = new Document();
        process(object, document);
        return document;
    }

}
