package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.AbstractIndexableFieldProcessor;
import com.namazustudios.socialengine.fts.FieldMetadata;
import org.apache.lucene.document.Document;

public class MongoFriendIdProcessor extends AbstractIndexableFieldProcessor<MongoFriendId> {

    @Override
    public void process(final Document document, final MongoFriendId value, final FieldMetadata field) {
        if (value != null ) {
            final String hexString = value.toHexString();
            newStringFields(document::add, hexString, field);
        }
    }

}
