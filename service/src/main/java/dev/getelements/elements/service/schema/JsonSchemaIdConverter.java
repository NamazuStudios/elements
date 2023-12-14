package dev.getelements.elements.service.schema;

import org.dozer.DozerConverter;

import static java.lang.String.format;

public class JsonSchemaIdConverter extends DozerConverter<String, String> {

    public JsonSchemaIdConverter() {
        super(String.class, String.class);
    }

    @Override
    public String convertTo(final String source, final String destination) {
        return doConvert(source);
    }

    @Override
    public String convertFrom(final String source, final String destination) {
        return doConvert(source);
    }

    private String doConvert(final String source) {
        final var apiOutsideUrl = getParameter();
        return source == null ? null : format("%s/metadata_spec/%s/schema.json", apiOutsideUrl, source);
    }

}
