package dev.getelements.elements.dao.mongo.converter;

import org.dozer.CustomConverter;

/**
 * Workaround for certain Dozer map issues where, if a Map contains another collection that has numbers, Dozer will
 * sometimes convert the numbers to strings. So, we just translate the field as-is from one object to the other.
 */
public class IdentityConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue,
            final Object sourceFieldValue,
            final Class<?> destinationClass,
            final Class<?> sourceClass) {
        return sourceFieldValue;
    }

}
