package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.FieldExtractionException;
import com.namazustudios.elements.fts.FieldMetadata;
import com.namazustudios.elements.fts.IndexableFieldExtractor;
import org.apache.lucene.document.Document;
import org.bson.types.ObjectId;

/**
 * Created by patricktwohig on 7/23/15.
 */
public class ObjectIdExtractor implements IndexableFieldExtractor<ObjectId> {

    @Override
    public ObjectId extract(final Document document, final FieldMetadata fieldMetadata) {

        final String objectIdString = document.get(fieldMetadata.name());

        try {
            return new ObjectId(objectIdString);
        } catch (IllegalArgumentException ex) {
            throw new FieldExtractionException(fieldMetadata, document);
        }

    }

}
