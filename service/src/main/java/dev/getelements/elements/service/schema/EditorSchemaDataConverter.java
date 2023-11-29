package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.util.*;

import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.OBJECT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

public class EditorSchemaDataConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue, final Object sourceFieldValue,
            final Class<?> destinationClass, final Class<?> sourceClass) {
        if (MetadataSpec.class.isAssignableFrom(sourceClass)) {
            final var spec = (MetadataSpec) sourceFieldValue;
            return spec == null ? null : new Converter(spec).convert();
        } else {
            throw new MappingException(format("Conversion does not exist from %s to %s.",
                    sourceClass.getName(),
                    destinationClass.getName()
            ));
        }

    }

    private static class Converter {

        private final MetadataSpec spec;

        private final Deque<Map<String, Object>> depth = new LinkedList<>();

        public Converter(final MetadataSpec spec) {
            this.spec = spec;
        }

        public Object convert() {

            final var type = spec.getType();

            if (OBJECT.equals(type)) {
                final var properties = spec.getProperties();
                return convert(properties);
            } else {
                return null;
            }

        }

        private Map<String, Object> convert(final List<MetadataSpecProperty> properties) {
            return properties == null ? null : properties
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(p -> OBJECT.equals(p.getType()) || p.getDefaultValue() != null)
                    .collect(toMap(MetadataSpecProperty::getName, this::convert));
        }

        private Object convert(final MetadataSpecProperty property) {

            final var type = property.getType();

            if (OBJECT.equals(type)) {
                final var properties = property.getProperties();
                return convert(properties);
            } else {
                return property.getDefaultValue();
            }

        }

    }

}
