package dev.getelements.elements.rt.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.InvocationTargetException;

public class KryoThrowableSerializer extends Serializer<Throwable> {

    @Override
    public void write(
            final Kryo kryo,
            final Output output,
            final Throwable object) {
        final var record = KryoThrowableContainer.from(object);
        final var serializer = kryo.getSerializer(KryoThrowableContainer.class);
        serializer.write(kryo, output, record);
    }

    @Override
    public Throwable read(
            final Kryo kryo,
            final Input input,
            final Class<Throwable> type) {
        final Serializer<KryoThrowableContainer> serializer = kryo.getSerializer(KryoThrowableContainer.class);
        final var record = serializer.read(kryo, input, KryoThrowableContainer.class);

        try {
            return record.toThrowable(kryo.getClassLoader());
        } catch (ClassNotFoundException |
                 NoSuchMethodException |
                 InvocationTargetException |
                 InstantiationException |
                 IllegalAccessException e) {
            throw new KryoException(e);
        }
    }

}
