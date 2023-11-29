package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.schema.MetadataSpecProperty;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class JsonSchemaRequiredPropertiesConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue, final Object sourceFieldValue,
            final Class<?> destinationClass, final Class<?> sourceClass) {
        if (List.class.isAssignableFrom(sourceClass) && List.class.isAssignableFrom(destinationClass)) {
            try {
                return sourceFieldValue == null ? null : ((List<?>) sourceFieldValue)
                        .stream()
                        .map(MetadataSpecProperty.class::cast)
                        .filter(MetadataSpecProperty::isRequired)
                        .map(MetadataSpecProperty::getName)
                        .collect(toList());
            } catch (ClassCastException ex) {
                throw new MappingException("Unexpected type.", ex);
            }
        } else {
            throw new MappingException("Both fields must be of list type.");
        }
    }

}
