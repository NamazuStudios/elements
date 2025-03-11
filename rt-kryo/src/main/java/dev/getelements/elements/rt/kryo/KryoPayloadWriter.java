package dev.getelements.elements.rt.kryo;

import com.esotericsoftware.kryo.io.Output;
import dev.getelements.elements.rt.PayloadWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class KryoPayloadWriter implements PayloadWriter {

    @Override
    public byte[] write(final Object payload) throws IOException {

        final var kryo = KryoInstance.get();

        try (final var bos = new ByteArrayOutputStream();
             final var output = new Output(bos)) {
            kryo.writeObject(output, payload);
            output.flush();
            return bos.toByteArray();
        }

    }

    @Override
    public void write(final Object payload, final OutputStream stream) throws IOException {
        final var kryo = KryoInstance.get();
        final var output = new Output(stream);
        kryo.writeObject(output, payload);
        output.flush();
    }

}
