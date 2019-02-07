package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.elements.fts.AbstractIndexableFieldProcessor;
import com.namazustudios.elements.fts.FieldMetadata;
import org.apache.lucene.document.Document;

public class MongoProgressIdProcessor extends AbstractIndexableFieldProcessor<MongoProgressId> {
    @Override
    public void process(final Document document, final MongoProgressId value, final FieldMetadata field) {
        if (value != null) {
            final String hexString = value.toHexString();
            newStringFields(document::add, hexString, field);
        }
    }
}
