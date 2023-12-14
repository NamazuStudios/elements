package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.schema.MetadataSpecPropertyType;
import org.dozer.DozerConverter;

import static dev.getelements.elements.model.schema.json.JsonSchemaType.getJsonSchemaType;
import static dev.getelements.elements.model.schema.json.JsonSchemaType.getMetadataSpecPropertyType;

public class JsonSchemaTypeConverter extends DozerConverter<MetadataSpecPropertyType, String> {

    public JsonSchemaTypeConverter() {
        super(MetadataSpecPropertyType.class, String.class);
    }

    @Override
    public String convertTo(final MetadataSpecPropertyType source, final String destination) {
        return source == null ? null : getJsonSchemaType(source);
    }

    @Override
    public MetadataSpecPropertyType convertFrom(final String source, final MetadataSpecPropertyType destination) {
        return source == null ? null : getMetadataSpecPropertyType(source);
    }

}
