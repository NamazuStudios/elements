package dev.getelements.elements.rt.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.rt.PayloadWriter;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Uses an instance of {@link ObjectMapper} to deserialize the payload from the {@link OutputStream}.
 */
public class ObjectMapperPayloadWriter implements PayloadWriter {

    private ObjectMapper objectMapper;

    @Override
    public void write(final Object payload, final OutputStream stream) throws IOException {
        getObjectMapper().writeValue(stream, payload);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
