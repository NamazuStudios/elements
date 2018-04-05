package com.namazustudios.socialengine.dao.mongo.converter;

import com.namazustudios.socialengine.dao.mongo.model.MongoFriendshipId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class MongoFriendIdConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == MongoFriendshipId.class && destinationClass == MongoFriendshipId.class) {
            return sourceFieldValue;
        } else if (sourceClass == MongoFriendshipId.class && destinationClass == String.class) {
            return sourceFieldValue == null ? null : ((MongoFriendshipId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass == MongoFriendshipId.class) {
            return sourceFieldValue == null ? null : new MongoFriendshipId((String)sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}
