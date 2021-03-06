package com.namazustudios.socialengine.dao.mongo.model.gameon;

import com.namazustudios.elements.fts.*;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import org.apache.lucene.document.Document;
import org.bson.types.ObjectId;
import org.dozer.CustomConverter;
import org.dozer.MappingException;
import dev.morphia.annotations.Property;

import java.util.Base64;
import java.util.Objects;

import static com.namazustudios.socialengine.dao.mongo.MongoConstants.OID_LENGTH_BYTES;
import static java.lang.System.arraycopy;

public class MongoGameOnSessionId {

    public static final byte VERSION = 0;

    public static final int VERSION_LENGTH_BYTES = 1;

    public static final int ORDINAL_LENGTH_BYTES = 1;

    public static final int LENGTH_BYTES = VERSION_LENGTH_BYTES + OID_LENGTH_BYTES + ORDINAL_LENGTH_BYTES;

    public static final int DEVICE_OS_ORDINAL_INDEX = VERSION_LENGTH_BYTES + OID_LENGTH_BYTES;

    private static final DeviceOSType[] DEVICE_OS_TYPES = DeviceOSType.values();

    @Property
    private ObjectId profileId;

    @Property
    private DeviceOSType deviceOSType;

    MongoGameOnSessionId() { /* needed for morphia */}

    public MongoGameOnSessionId(final String string) {

        final byte[] bytes = Base64.getDecoder().decode(string);
        if (bytes.length != LENGTH_BYTES) throw new IllegalArgumentException("Invalid length");
        if (bytes[0] != VERSION) throw new IllegalArgumentException("Invalid version: " + bytes[0]);

        final byte[] profileIdBytes = new byte[OID_LENGTH_BYTES];
        arraycopy(bytes, 1, profileIdBytes, 0, OID_LENGTH_BYTES);
        profileId = new ObjectId(profileIdBytes);

        final int deviceOSTypeOrdinal = bytes[DEVICE_OS_ORDINAL_INDEX];
        if (deviceOSTypeOrdinal > DEVICE_OS_TYPES.length) throw new IllegalArgumentException("Invalid OS Ordinal: " + deviceOSTypeOrdinal);
        deviceOSType = DEVICE_OS_TYPES[deviceOSTypeOrdinal];

    }

    public MongoGameOnSessionId(final ObjectId profileId, final DeviceOSType deviceOSType) {
        this.profileId = profileId;
        this.deviceOSType = deviceOSType;
    }

    public ObjectId getProfileId() {
        return profileId;
    }

    public DeviceOSType getDeviceOSType() {
        return deviceOSType;
    }

    public byte[] toByteArray() {

        final byte[] bytes = new byte[LENGTH_BYTES];

        bytes[0] = VERSION;
        bytes[DEVICE_OS_ORDINAL_INDEX] = (byte) deviceOSType.ordinal();

        final byte[] objectIdBytes = profileId.toByteArray();
        arraycopy(objectIdBytes, 0, bytes, 1, objectIdBytes.length);

        return bytes;
    }

    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoGameOnSessionId)) return false;
        MongoGameOnSessionId that = (MongoGameOnSessionId) object;
        return Objects.equals(getProfileId(), that.getProfileId()) &&
                getDeviceOSType() == that.getDeviceOSType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProfileId(), getDeviceOSType());
    }

    @Override
    public String toString() {
        return "MongoGameOnSessionId{" +
                "profileId=" + profileId +
                ", deviceOSType=" + deviceOSType +
                '}';
    }

    public static class Extractor implements IndexableFieldExtractor<MongoGameOnSessionId> {
        @Override
        public MongoGameOnSessionId extract(final Document document, final FieldMetadata fieldMetadata) {
            final String sessionIdString = document.get(fieldMetadata.name());

            try {
                return new MongoGameOnSessionId(sessionIdString);
            } catch (IllegalArgumentException ex) {
                throw new FieldExtractionException(fieldMetadata, document);
            }
        }
    }

    public static class Processor extends AbstractIndexableFieldProcessor<MongoGameOnSessionId> {
        @Override
        public void process(final Document document, final MongoGameOnSessionId value, final FieldMetadata field) {
            if (value != null) {
                final String hexString = value.toHexString();
                newStringFields(document::add, hexString, field);
            }
        }
    }

    public static class Converter implements CustomConverter {
        @Override
        public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
            if (sourceClass == MongoGameOnSessionId.class && destinationClass == MongoGameOnSessionId.class) {
                return sourceFieldValue;
            } else if (sourceClass == MongoGameOnSessionId.class && destinationClass == String.class) {
                return sourceFieldValue == null ? null : ((MongoGameOnSessionId) sourceFieldValue).toHexString();
            } else if (sourceClass == String.class && destinationClass == MongoGameOnSessionId.class) {
                return sourceFieldValue == null ? null : new MongoGameOnSessionId((String)sourceFieldValue);
            } else {
                throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
            }
        }
    }
}
