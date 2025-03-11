package dev.getelements.elements.rt.kryo;

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

    public static void main(String[] args) {
        
        final var bos = new ByteArrayOutputStream();
        final var output = new Output(bos);
        get().writeObject(output, new Exception("Test", new RuntimeException()));
        output.flush();

        final var bytes = bos.toByteArray();
        final var bis = new ByteArrayInputStream(bytes);
        final var input = new Input(bis);
        final var result = get().readObject(input, Throwable.class);
        result.printStackTrace(System.out);

    }

}
