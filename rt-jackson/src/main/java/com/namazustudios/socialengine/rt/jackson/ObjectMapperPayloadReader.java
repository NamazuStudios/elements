package com.namazustudios.socialengine.rt.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.PayloadReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Uses an instance of {@linl ObjectMapper} to deserialize the payload from the {@link InputStream}.
 */
public class ObjectMapperPayloadReader implements PayloadReader {

    private ObjectMapper objectMapper;

    @Override
    public Object read(final Class<?> payloadType, final InputStream stream) throws IOException {
        return getObjectMapper().readValue(stream, payloadType);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
