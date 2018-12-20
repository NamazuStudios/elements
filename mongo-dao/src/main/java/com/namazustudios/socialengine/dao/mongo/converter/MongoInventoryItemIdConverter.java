package com.namazustudios.socialengine.dao.mongo.converter;

import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class MongoInventoryItemIdConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == MongoInventoryItemId.class && destinationClass == MongoInventoryItemId.class) {
            return sourceFieldValue;
        } else if (sourceClass == MongoInventoryItemId.class && destinationClass == String.class) {
            return sourceFieldValue == null ? null : ((MongoInventoryItemId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass == MongoInventoryItemId.class) {
            return sourceFieldValue == null ? null : new MongoInventoryItemId((String)sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}
