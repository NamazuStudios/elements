package com.namazustudios.socialengine.dao.mongo.converter;

import org.bson.types.ObjectId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

/**
 * Converts instances of {@link org.bson.types.ObjectId} to {@link String} and back.
 *
 * Created by patricktwohig on 5/25/17.
 */
public class ObjectIdConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue, final Object sourceFieldValue,
            final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == ObjectId.class && destinationClass == ObjectId.class) {
            return sourceFieldValue;
        } else if (sourceClass == ObjectId.class && destinationClass == String.class) {
            return sourceFieldValue == null ? null : ((ObjectId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass == ObjectId.class) {
            return sourceFieldValue == null ? null : new ObjectId((String)sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}
