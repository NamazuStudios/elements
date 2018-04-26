package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.FieldExtractionException;
import com.namazustudios.elements.fts.FieldMetadata;
import com.namazustudios.elements.fts.IndexableFieldExtractor;
import org.apache.lucene.document.Document;
import org.bson.types.ObjectId;

public class MongoScoreIdExtractor implements IndexableFieldExtractor<MongoScoreId> {

    @Override
    public MongoScoreId extract(final Document document, final FieldMetadata fieldMetadata) {

        final String scoreIdString = document.get(fieldMetadata.name());

        try {
            return new MongoScoreId(scoreIdString);
        } catch (IllegalArgumentException ex) {
            throw new FieldExtractionException(fieldMetadata, document);
        }

    }

}
