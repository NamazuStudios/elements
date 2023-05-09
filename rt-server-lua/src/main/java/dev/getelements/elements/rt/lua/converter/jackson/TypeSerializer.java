package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.getelements.elements.rt.manifest.model.Type;

import java.io.IOException;

public class TypeSerializer extends JsonSerializer<Type> {

    @Override
    public void serialize(final Type value,
                          final JsonGenerator gen,
                          final SerializerProvider serializers) throws IOException, JsonProcessingException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.value);
        }
    }

}
