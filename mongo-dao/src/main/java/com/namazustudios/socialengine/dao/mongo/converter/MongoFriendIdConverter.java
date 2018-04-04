package com.namazustudios.socialengine.dao.mongo.converter;

import com.namazustudios.socialengine.dao.mongo.model.MongoFriendId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class MongoFriendIdConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == MongoFriendId.class && destinationClass == MongoFriendId.class) {
            return sourceFieldValue;
        } else if (sourceClass == MongoFriendId.class && destinationClass == String.class) {
            return sourceFieldValue == null ? null : ((MongoFriendId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass == MongoFriendId.class) {
            return sourceFieldValue == null ? null : new MongoFriendId((String)sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}
