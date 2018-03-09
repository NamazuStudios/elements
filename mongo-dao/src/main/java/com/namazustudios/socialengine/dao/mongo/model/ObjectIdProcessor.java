package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.AbstractIndexableFieldProcessor;
import com.namazustudios.socialengine.fts.FieldMetadata;
import org.apache.lucene.document.Document;
import org.bson.types.ObjectId;

/**
 * Created by patricktwohig on 7/23/15.
 */
public class ObjectIdProcessor extends AbstractIndexableFieldProcessor<ObjectId> {

    @Override
    public void process(final Document document, final ObjectId value, final FieldMetadata field) {
        if (value != null ) {
            final String hexString = value.toHexString();
            newStringFields(document::add, hexString, field);
        }
    }

}
