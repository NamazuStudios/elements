package com.namazustudios.socialengine.dao.mongo.converter;

import com.namazustudios.socialengine.dao.mongo.HexableId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.lang.reflect.InvocationTargetException;

public class MongoHexableIdConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue,
                          final Object sourceFieldValue,
                          final Class<?> destinationClass,
                          final Class<?> sourceClass) {
        if (destinationClass.isAssignableFrom(sourceClass)) {
            return sourceFieldValue;
        } else if (HexableId.class.isAssignableFrom(sourceClass) && destinationClass.isAssignableFrom(String.class)) {
            return sourceFieldValue == null ? null : ((HexableId) sourceFieldValue).toHexString();
        } else if (sourceClass == String.class && destinationClass.isAssignableFrom(HexableId.class)) {
            return reflectionConversion(sourceFieldValue, destinationClass, sourceClass);
        } else {
            throw new MappingException("No conversion exists between " + sourceClass + " and " + destinationClass);
        }
    }

    private Object reflectionConversion(final Object sourceFieldValue,
                                        final Class<?> destinationClass,
                                        final Class<?> sourceClass) {
        try {
            final var ctor = destinationClass.getConstructor(String.class);
            return ctor.newInstance(sourceFieldValue);
        } catch (NoSuchMethodException  |
                 InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException e) {
            throw new MappingException("No conversion exists between " + sourceClass + " and " + destinationClass, e);
        }
    }

}
