package com.namazustudios.socialengine.rt.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.PayloadWriter;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;

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
