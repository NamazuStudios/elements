package dev.getelements.elements.cluster.common.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class KryoInstance {

    private static final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> {
        final var kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.addDefaultSerializer(UUID.class, new KryoUUIDSerializer());
        kryo.addDefaultSerializer(Throwable.class, new KryoThrowableSerializer());
        return kryo;
    });

    public static Kryo get() {
        return kryo.get();
    }

}
