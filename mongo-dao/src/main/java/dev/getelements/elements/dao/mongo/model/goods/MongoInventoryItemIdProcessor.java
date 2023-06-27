package dev.getelements.elements.dao.mongo.model.goods;

import com.namazustudios.elements.fts.AbstractIndexableFieldProcessor;
import com.namazustudios.elements.fts.FieldMetadata;
import org.apache.lucene.document.Document;

public class MongoInventoryItemIdProcessor extends AbstractIndexableFieldProcessor<MongoInventoryItemId> {
    @Override
    public void process(final Document document, final MongoInventoryItemId value, final FieldMetadata field) {
        if (value != null) {
            final String hexString = value.toHexString();
            newStringFields(document::add, hexString, field);
        }
    }
}
