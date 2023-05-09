package dev.getelements.elements.dao.mongo.converter;

import dev.getelements.elements.dao.mongo.model.mission.MongoProgressId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class MongoProgressIdConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == MongoProgressId.class && destinationClass == MongoProgressId.class) {
            return sourceFieldValue;
        } else if (sourceClass == MongoProgressId.class && destinationClass == String.class) {
            return sourceFieldValue == null ? null : ((MongoProgressId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass == MongoProgressId.class) {
            return sourceFieldValue == null ? null : new MongoProgressId((String)sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}
