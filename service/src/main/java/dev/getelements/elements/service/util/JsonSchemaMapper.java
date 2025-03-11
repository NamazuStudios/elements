package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.MetadataSpecProperty;
import dev.getelements.elements.sdk.model.schema.json.JsonSchema;
import dev.getelements.elements.sdk.model.schema.json.JsonSchemaProperty;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

import static dev.getelements.elements.sdk.model.schema.json.JsonSchemaType.getJsonSchemaType;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Mapper
public abstract class JsonSchemaMapper implements MapperRegistry.Mapper<MetadataSpec, JsonSchema> {

    private String apiOutsideUrl;

    @Override
    @Mapping(target = "title", source = "name")
    @Mapping(target = "description", source = "name")
    @Mapping(target = "required", source = "properties")
    @Mapping(target = "properties", source = "properties")
    public abstract JsonSchema forward(MetadataSpec source);

    protected List<String> toRequiredProperties(final List<MetadataSpecProperty> metadataSpecPropertyList) {
                return metadataSpecPropertyList == null
                        ? null
                        : metadataSpecPropertyList
                            .stream()
                            .map(MetadataSpecProperty.class::cast)
                            .filter(MetadataSpecProperty::isRequired)
                            .map(MetadataSpecProperty::getName)
                            .collect(toList());
    }

    protected Map<String, JsonSchemaProperty> toOptionalProperties(final List<MetadataSpecProperty> metadataSpecPropertyList) {
        return metadataSpecPropertyList
                        .stream()
                        .collect(toMap(MetadataSpecProperty::getName, this::convert));
    }

    private JsonSchemaProperty convert(
            final MetadataSpecProperty property) {

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
                    .collect(toMap(MetadataSpecProperty::getName, this::convert));

            jsonSchemaProperty.setRequired(required);
            jsonSchemaProperty.setProperties(jsonSchemaProperties);

        }

        return jsonSchemaProperty;

    }

    @AfterMapping
    protected void setJsonSchemaId(final MetadataSpec metadataSpec, @MappingTarget final JsonSchema jsonSchema) {
        final var jsonSchemaId = format("%s/metadata_spec/%s/schema.json", getApiOutsideUrl(), metadataSpec.getId());
        jsonSchema.set$id(jsonSchemaId);
    }

    public String getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    public void setApiOutsideUrl(String apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

}
