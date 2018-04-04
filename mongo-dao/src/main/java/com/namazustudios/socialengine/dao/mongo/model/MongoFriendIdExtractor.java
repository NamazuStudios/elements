package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.FieldExtractionException;
import com.namazustudios.socialengine.fts.FieldMetadata;
import com.namazustudios.socialengine.fts.IndexableFieldExtractor;
import org.apache.lucene.document.Document;
import org.bson.types.ObjectId;

public class MongoFriendIdExtractor implements IndexableFieldExtractor<MongoFriendId> {

    @Override
    public MongoFriendId extract(final Document document, final FieldMetadata fieldMetadata) {

        final String friendIdString = document.get(fieldMetadata.name());

        try {
            return new MongoFriendId(friendIdString);
        } catch (IllegalArgumentException ex) {
            throw new FieldExtractionException(fieldMetadata, document);
        }

    }

}
