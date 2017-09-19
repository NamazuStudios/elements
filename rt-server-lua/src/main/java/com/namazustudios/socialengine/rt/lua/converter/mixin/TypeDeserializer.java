package com.namazustudios.socialengine.rt.lua.converter.mixin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import java.io.IOException;

public class TypeDeserializer extends JsonDeserializer<Type> {

    @Override
    public Type deserialize(final JsonParser p,
                            final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final String value;

        try {
            value = p.getValueAsString().toLowerCase();
        } catch (NullPointerException ex) {
            throw new JsonParseException(p, "expected non null value for type", p.getCurrentLocation(), ex);
        }

        try {
            return Type.findByValue(value);
        } catch (BadManifestException ex) {
            throw new JsonParseException(p, "expected non null value for type", p.getCurrentLocation(), ex);
        }

    }

}
