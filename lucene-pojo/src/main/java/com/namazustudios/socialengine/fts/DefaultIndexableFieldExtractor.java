package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * Created by patricktwohig on 5/14/15.
 */
public class DefaultIndexableFieldExtractor implements IndexableFieldExtractor<Object> {

    @Override
    public Object extract(Document document, FieldMetadata field) {
        // TODO Implement
        return null;
    }

}
