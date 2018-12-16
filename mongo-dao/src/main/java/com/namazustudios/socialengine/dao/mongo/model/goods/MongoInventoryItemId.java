package com.namazustudios.socialengine.dao.mongo.model.goods;

import com.namazustudios.socialengine.exception.NotFoundException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;

import java.util.Base64;
import java.util.Objects;

import static java.lang.System.arraycopy;

@Embedded
public class MongoInventoryItemId {

    private static final int OBJECT_ID_LENGTH = 12;

    private ObjectId itemObjectId;

    private int priority;

    public MongoInventoryItemId() {}

    public MongoInventoryItemId(final String hexString) {

        if (hexString == null) throw new IllegalArgumentException("must specify inventory item id string");

        final byte [] bytes = Base64.getDecoder().decode(hexString);
        if (bytes.length != OBJECT_ID_LENGTH + Integer.BYTES) throw new IllegalArgumentException();

        final byte[] objectIdBytes = new byte[OBJECT_ID_LENGTH];
        arraycopy(bytes, 0, objectIdBytes, 0, objectIdBytes.length);

        priority |= bytes[OBJECT_ID_LENGTH + 0] << (0 * Byte.SIZE);
        priority |= bytes[OBJECT_ID_LENGTH + 1] << (1 * Byte.SIZE);
        priority |= bytes[OBJECT_ID_LENGTH + 2] << (2 * Byte.SIZE);
        priority |= bytes[OBJECT_ID_LENGTH + 3] << (3 * Byte.SIZE);

    }

    public MongoInventoryItemId(final ObjectId itemObjectId, final int priority) {
        if (priority < 0) throw new IllegalArgumentException("Invalid priority: " + priority);
        if (itemObjectId == null) throw new IllegalArgumentException("object id must not be null");
        this.itemObjectId = itemObjectId;
        this.priority = priority;
    }

    public ObjectId getItemObjectId() {
        return itemObjectId;
    }

    public void setItemObjectId(ObjectId itemObjectId) {
        this.itemObjectId = itemObjectId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public byte[] toByteArray() {

        final byte[] objectIdBytes = itemObjectId.toByteArray();
        final byte[] bytes = new byte[Integer.BYTES + objectIdBytes.length];

        arraycopy(objectIdBytes, 0, bytes, 0, objectIdBytes.length);

        bytes[OBJECT_ID_LENGTH + 0] = (byte)(0xFF & (priority >> Byte.SIZE * 0));
        bytes[OBJECT_ID_LENGTH + 1] = (byte)(0xFF & (priority >> Byte.SIZE * 1));
        bytes[OBJECT_ID_LENGTH + 2] = (byte)(0xFF & (priority >> Byte.SIZE * 2));
        bytes[OBJECT_ID_LENGTH + 3] = (byte)(0xFF & (priority >> Byte.SIZE * 3));

        return objectIdBytes;
    }

    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoInventoryItemId)) return false;
        MongoInventoryItemId that = (MongoInventoryItemId) object;
        return getPriority() == that.getPriority() &&
                Objects.equals(getItemObjectId(), that.getItemObjectId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemObjectId(), getPriority());
    }

    @Override
    public String toString() {
        return "MongoInventoryItemId{" +
                "itemObjectId=" + itemObjectId +
                ", priority=" + priority +
                '}';
    }

    public static MongoInventoryItemId parseOrThrowNotFoundException(final String inventoryItemId) {
        try {
            return new MongoInventoryItemId(inventoryItemId);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ex);
        }
    }

}
