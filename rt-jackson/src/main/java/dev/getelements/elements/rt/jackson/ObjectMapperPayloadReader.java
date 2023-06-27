package dev.getelements.elements.rt.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import dev.getelements.elements.rt.PayloadReader;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.io.ByteStreams.toByteArray;

/**
 * Uses an instance of {@linl ObjectMapper} to deserialize the payload from the {@link InputStream}.
 */
public class ObjectMapperPayloadReader implements PayloadReader {

    private ObjectMapper objectMapper;

    @Override
    public <T> T convert(final Class<T> to, final Object from) {
        return getObjectMapper().convertValue(from, to);
    }

    @Override
    public <T> T read(final Class<T> payloadType, final InputStream stream) throws IOException {
        final byte[] bytes = toByteArray(stream);
        return bytes.length == 0 ? null : getObjectMapper().readValue(bytes, payloadType);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static void main(String[] args) {
        final var om = new ObjectMapper();
        final var converted = om.convertValue(new Object[] { "Foo", "Bar", 1, 3L, 4.2 }, List.class);
        System.out.println("Converted: " + converted);
    }

}
