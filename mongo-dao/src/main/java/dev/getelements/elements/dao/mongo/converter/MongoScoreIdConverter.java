package dev.getelements.elements.dao.mongo.converter;

import dev.getelements.elements.dao.mongo.model.MongoScoreId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class MongoScoreIdConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == MongoScoreId.class && destinationClass == MongoScoreId.class) {
            return sourceFieldValue;
        } else if (sourceClass == MongoScoreId.class && destinationClass == String.class) {
            return sourceFieldValue == null ? null : ((MongoScoreId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass == MongoScoreId.class) {
            return sourceFieldValue == null ? null : new MongoScoreId((String)sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}
