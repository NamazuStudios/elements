package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.HexableId;
import dev.getelements.elements.rt.util.Hex;
import org.bson.types.ObjectId;
import org.mapstruct.TargetType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

public class PropertyConverters {

    private static Map<Class<?>, Constructor<?>> CTOR_CACHE = new ConcurrentHashMap<>();

    public String toHexString(final ObjectId objectId) {
        return objectId == null ? null : objectId.toHexString();
    }

    public ObjectId toObjectId(final String objectIdString) {
        return objectIdString == null ? null : new ObjectId(objectIdString);
    }

    public String toHexString(final byte[] bytes) {
        return bytes == null ? null : Hex.encode(bytes);
    }

    public byte[] toByteArray(final String string) {
        return string == null ? null : Hex.decode(string);
    }

    public String toHexString(final HexableId hexableId) {
        return hexableId == null ? null : hexableId.toHexString();
    }

    public Long toLong(@TargetType final Class<? extends Long> longType, final Timestamp timestamp) {
        if (longType.isPrimitive()) {
            return timestamp == null ? 0 : timestamp.toInstant().toEpochMilli();
        } else {
            return timestamp == null ? null : timestamp.toInstant().toEpochMilli();
        }
    }

    public Timestamp toTimestamp(final Long timestamp) {
        return timestamp == null ? null : new Timestamp(timestamp);
    }

    public <HexableIdT extends HexableId> HexableIdT toHexableId(
            final String string,
            final @TargetType Class<HexableIdT> hexableIdTClass) {

        if (string == null) {
            return null;
        }

        try {
            final var ctor = getHexableConstructor(hexableIdTClass);
            return ctor.newInstance(string);
        } catch (ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalArgumentException(format("No conversion exists between %s and %s",
                    String.class.getSimpleName(),
                    hexableIdTClass.getSimpleName()
            ), ex);
        }

    }

    private static <HexableIdT extends HexableId>
    Constructor<HexableIdT> getHexableConstructor(final Class<HexableIdT> hexableIdTClass) {
        return (Constructor<HexableIdT>) CTOR_CACHE.computeIfAbsent(hexableIdTClass, c -> {
            try {
                return hexableIdTClass.getConstructor(String.class);
            } catch (ClassCastException | NoSuchMethodException ex) {
                throw new IllegalArgumentException(format("No conversion exists between %s and %s",
                        String.class.getSimpleName(),
                        hexableIdTClass.getSimpleName()
                ), ex);
            }
        });
    }

}
