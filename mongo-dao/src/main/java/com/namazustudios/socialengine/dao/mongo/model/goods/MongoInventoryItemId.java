package com.namazustudios.socialengine.dao.mongo.model.goods;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.NotFoundException;
import org.bson.types.ObjectId;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.Base64;
import java.util.Objects;

import static java.lang.System.arraycopy;

@Embedded
public class MongoInventoryItemId {

    private static final int USER_ID_INDEX = 0;

    private static final int ITEM_ID_INDEX = 1;

    private static final int OBJECT_ID_LENGTH = 12;

    private static final int PRIORITY_OFFSET = OBJECT_ID_LENGTH * 2;

    private static final int INVENTORY_ITEM_ID_LENGTH = PRIORITY_OFFSET + Integer.BYTES;

    @Property
    private ObjectId userObjectId;

    @Property
    private ObjectId itemObjectId;

    @Property
    private int priority;

    public MongoInventoryItemId() {}

    public MongoInventoryItemId(final String hexString) {

        if (hexString == null) throw new IllegalArgumentException("must specify inventory item id string");

        final byte [] bytes = Base64.getDecoder().decode(hexString);
        if (bytes.length != (OBJECT_ID_LENGTH * 2) + Integer.BYTES) throw new IllegalArgumentException();

        final byte[] objectIdBytes = new byte[OBJECT_ID_LENGTH];

        arraycopy(bytes, OBJECT_ID_LENGTH * USER_ID_INDEX, objectIdBytes, 0, objectIdBytes.length);
        userObjectId = new ObjectId(objectIdBytes);

        arraycopy(bytes, OBJECT_ID_LENGTH * ITEM_ID_INDEX, objectIdBytes, 0, objectIdBytes.length);
        itemObjectId = new ObjectId(objectIdBytes);

        priority |= bytes[PRIORITY_OFFSET + 0] << (0 * Byte.SIZE);
        priority |= bytes[PRIORITY_OFFSET + 1] << (1 * Byte.SIZE);
        priority |= bytes[PRIORITY_OFFSET + 2] << (2 * Byte.SIZE);
        priority |= bytes[PRIORITY_OFFSET + 3] << (3 * Byte.SIZE);

    }

    public MongoInventoryItemId(final MongoUser mongoUser, final MongoItem mongoItem, final int priority) {
        this(mongoUser.getObjectId(), mongoItem.getObjectId(), priority);
    }

    public MongoInventoryItemId(final ObjectId userObjectId, final ObjectId itemObjectId, final int priority) {
        if (priority < 0) throw new IllegalArgumentException("Invalid priority: " + priority);
        if (userObjectId == null) throw new IllegalArgumentException("object id must not be null");
        if (itemObjectId == null) throw new IllegalArgumentException("object id must not be null");
        this.userObjectId = userObjectId;
        this.itemObjectId = itemObjectId;
        this.priority = priority;
    }

    public ObjectId getUserObjectId() {
        return userObjectId;
    }

    public void setUserObjectId(ObjectId userObjectId) {
        this.userObjectId = userObjectId;
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

        final byte[] userIdBytes = userObjectId.toByteArray();
        final byte[] itemIdBytes = itemObjectId.toByteArray();
        final byte[] bytes = new byte[INVENTORY_ITEM_ID_LENGTH];

        arraycopy(userIdBytes, 0, bytes, OBJECT_ID_LENGTH * USER_ID_INDEX, userIdBytes.length);
        arraycopy(itemIdBytes, 0, bytes, OBJECT_ID_LENGTH * ITEM_ID_INDEX, itemIdBytes.length);

        bytes[PRIORITY_OFFSET + 0] = (byte)(0xFF & (priority >> Byte.SIZE * 0));
        bytes[PRIORITY_OFFSET + 1] = (byte)(0xFF & (priority >> Byte.SIZE * 1));
        bytes[PRIORITY_OFFSET + 2] = (byte)(0xFF & (priority >> Byte.SIZE * 2));
        bytes[PRIORITY_OFFSET + 3] = (byte)(0xFF & (priority >> Byte.SIZE * 3));

        return bytes;

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
                "userObjectId=" + userObjectId +
                ", itemObjectId=" + itemObjectId +
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
