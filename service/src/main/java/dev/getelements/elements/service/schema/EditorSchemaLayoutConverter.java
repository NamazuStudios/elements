package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.schema.MetadataSpecPropertyType;
import dev.getelements.elements.model.schema.layout.EditorLayout;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.ARRAY;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.OBJECT;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;

public class EditorSchemaLayoutConverter implements CustomConverter {

    @Override
    public Object convert(
            final Object existingDestinationFieldValue, final Object sourceFieldValue,
            final Class<?> destinationClass, final Class<?> sourceClass) {
        if (MetadataSpec.class.isAssignableFrom(sourceClass) && List.class.isAssignableFrom(destinationClass)) {
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

        private final Deque<MetadataSpecProperty> jsonPath = new LinkedList<>();

        public Converter(final MetadataSpec spec) {
            this.spec = spec;
        }

        public List<EditorLayout> convert() {
            final var properties = spec.getProperties();
            return properties == null ? null : convert(properties).collect(toList());
        }

        private Stream<EditorLayout> convert(final MetadataSpecProperty property) {

            jsonPath.addLast(property);

            try {

                final var key = jsonPath
                        .stream()
                        .map(MetadataSpecProperty::getName)
                        .collect(joining("."));

                final var layout = new EditorLayout();
                layout.setKey(key);
                layout.setTitle(property.getDisplayName());
                layout.setPlaceholder(property.getPlaceholder());

                return Stream.concat(Stream.of(layout), convert(property.getProperties()));

            } finally {
                jsonPath.removeLast();
            }

        }

        private Stream<EditorLayout> convert(final List<MetadataSpecProperty> properties) {
            return properties == null ? empty() : properties
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(o -> o.getName() != null && !ARRAY.equals(o.getType()))
                    .flatMap(this::convert)
                    .collect(toList())
                    .stream();
        }

    }

}
