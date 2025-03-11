package dev.getelements.elements.rt.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.getelements.elements.rt.PayloadReader;

import java.io.IOException;
import java.io.InputStream;

public class KryoPayloadReader implements PayloadReader {

    @Override
    public <T> T convert(final Class<T> to, final Object from) {

        final var kryo = KryoInstance.get();

        try (final var output = new Output()) {

            kryo.writeObject(output, from);

            try (final var input = new Input(output.toBytes())) {
                return kryo.readObject(input, to);
            }

        }

    }

    @Override
    public <T> T read(final Class<T> payloadType, final InputStream stream) throws IOException {
        final var bytes = stream.readAllBytes();
        final var input = new Input(bytes);
        return KryoInstance.get().readObject(input, payloadType);
    }

}
