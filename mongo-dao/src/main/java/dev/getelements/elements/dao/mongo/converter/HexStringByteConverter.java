package dev.getelements.elements.dao.mongo.converter;

import dev.getelements.elements.rt.util.Hex;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class HexStringByteConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue,
            final Object sourceFieldValue,
            final Class<?> destinationClass,
            final Class<?> sourceClass) {
        if (destinationClass.isAssignableFrom(sourceClass)) {
            return sourceFieldValue;
        } else if (String.class.isAssignableFrom(sourceClass) && destinationClass.isAssignableFrom(byte[].class)) {
            return Hex.decode((String) sourceFieldValue);
        } else if (byte[].class.isAssignableFrom(sourceClass) && destinationClass.isAssignableFrom(String.class)) {
            return Hex.encode((byte[]) sourceFieldValue);
        } else {
            throw new MappingException("No conversion exists between " + sourceClass + " and " + destinationClass);
        }
    }

}

