package dev.getelements.elements.rt.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.UUID;

public class KryoUUIDSerializer extends Serializer<UUID> {

    @Override
    public UUID read(final Kryo kryo, final Input input, final Class<UUID> type) {
        final var msb = input.readLong();
        final var lsb = input.readLong();
        return new UUID(msb, lsb);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final UUID object) {
        final var msb = object.getMostSignificantBits();
        final var lsb = object.getLeastSignificantBits();
        output.writeLong(msb);
        output.writeLong(lsb);
    }

}
