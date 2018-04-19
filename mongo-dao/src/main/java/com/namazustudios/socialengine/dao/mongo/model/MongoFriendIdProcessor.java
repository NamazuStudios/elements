package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.AbstractIndexableFieldProcessor;
import com.namazustudios.elements.fts.FieldMetadata;
import org.apache.lucene.document.Document;

public class MongoFriendIdProcessor extends AbstractIndexableFieldProcessor<MongoFriendshipId> {

    @Override
    public void process(final Document document, final MongoFriendshipId value, final FieldMetadata field) {
        if (value != null ) {
            final String hexString = value.toHexString();
            newStringFields(document::add, hexString, field);
        }
    }

}
