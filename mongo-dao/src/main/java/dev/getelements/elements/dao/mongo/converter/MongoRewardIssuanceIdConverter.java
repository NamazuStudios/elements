package dev.getelements.elements.dao.mongo.converter;

import dev.getelements.elements.dao.mongo.model.mission.MongoRewardIssuanceId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class MongoRewardIssuanceIdConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == MongoRewardIssuanceId.class && destinationClass == MongoRewardIssuanceId.class) {
            return sourceFieldValue;
        } else if (sourceClass == MongoRewardIssuanceId.class && destinationClass == String.class) {
            return sourceFieldValue == null ? null : ((MongoRewardIssuanceId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass == MongoRewardIssuanceId.class) {
            return sourceFieldValue == null ? null : new MongoRewardIssuanceId((String)sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists between " + sourceClass + " and " + destinationClass);
        }
    }

}
