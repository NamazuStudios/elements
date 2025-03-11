package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.model.schema.EditorSchema;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.MetadataSpecProperty;
import dev.getelements.elements.sdk.model.schema.layout.EditorLayout;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType.ARRAY;
import static dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType.OBJECT;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.empty;

@Mapper
public abstract class EditorSchemaMapper implements MapperRegistry.Mapper<MetadataSpec, EditorSchema> {

    private final JsonSchemaMapper jsonSchemaMapper = Mappers.getMapper(JsonSchemaMapper.class);

    @Override
    public abstract EditorSchema forward(MetadataSpec source);

    @AfterMapping
    protected void mapSchema(
            @MappingTarget
            final EditorSchema editorSchema,
            final MetadataSpec metadataSpec) {

        final var data = new DataConverter(metadataSpec).convert();
        final var schema = jsonSchemaMapper.forward(metadataSpec);
        final var layout = new EditorLayoutConverter(metadataSpec).convert();

        editorSchema.setData(data);
        editorSchema.setSchema(schema);
        editorSchema.setLayout(layout);

    }

    public String getApiOutsideUrl() {
        return jsonSchemaMapper.getApiOutsideUrl();
    }

    public void setApiOutsideUrl(String apiOutsideUrl) {
        jsonSchemaMapper.setApiOutsideUrl(apiOutsideUrl);
    }

    private static class DataConverter {

        private final MetadataSpec spec;

        private final Deque<Map<String, Object>> depth = new LinkedList<>();

        public DataConverter(final MetadataSpec spec) {
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


    private static class EditorLayoutConverter {

        private final MetadataSpec spec;

        private final Deque<MetadataSpecProperty> jsonPath = new LinkedList<>();

        public EditorLayoutConverter(final MetadataSpec spec) {
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
