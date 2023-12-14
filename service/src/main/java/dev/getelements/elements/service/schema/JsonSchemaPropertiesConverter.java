package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.schema.json.JsonSchemaProperty;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.getelements.elements.model.schema.json.JsonSchemaType.getJsonSchemaType;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class JsonSchemaPropertiesConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue, final Object sourceFieldValue,
            final Class<?> destinationClass, final Class<?> sourceClass) {
        if (Map.class.isAssignableFrom(destinationClass) && List.class.isAssignableFrom(sourceClass)) {
            try {
                return ((List<?>)sourceFieldValue)
                        .stream()
                        .map(MetadataSpecProperty.class::cast)
                        .collect(Collectors.toMap(MetadataSpecProperty::getName, this::convert));
            } catch (ClassCastException ex) {
                throw new MappingException("Encountered unknown type in source list.", ex);
            }
        } else {
            throw new MappingException(format("Conversion does not exist from %s to %s.",
                    sourceClass.getName(),
                    destinationClass.getName()
            ));
        }
    }

    private JsonSchemaProperty convert(final MetadataSpecProperty property) {

        final var jsonSchemaProperty = new JsonSchemaProperty();
        jsonSchemaProperty.setTitle(property.getDisplayName());
        jsonSchemaProperty.setDescription(property.getDisplayName());
        jsonSchemaProperty.setType(getJsonSchemaType(property.getType()));

        final var properties = property.getProperties();

        if (properties != null) {

            final var required = properties
                    .stream()
                    .map(MetadataSpecProperty.class::cast)
                    .filter(MetadataSpecProperty::isRequired)
                    .map(MetadataSpecProperty::getName)
                    .collect(toList());

            final var jsonSchemaProperties = properties
                    .stream()
                    .collect(Collectors.toMap(MetadataSpecProperty::getName, this::convert));

            jsonSchemaProperty.setRequired(required);
            jsonSchemaProperty.setProperties(jsonSchemaProperties);

        }

        return jsonSchemaProperty;

    }

}
