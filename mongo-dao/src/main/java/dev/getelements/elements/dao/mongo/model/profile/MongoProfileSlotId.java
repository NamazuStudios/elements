package dev.getelements.elements.dao.mongo.model.profile;

import dev.getelements.elements.dao.mongo.HexableId;
import dev.getelements.elements.sdk.util.Hex;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A compound ID for a {@link MongoProfileSlot}, encoding the owning user, the application, and the 0-based slot
 * number in fixed positions. Unlike {@link dev.getelements.elements.dao.mongo.model.MongoFriendshipId}, this is
 * <strong>not</strong> order-independent -- user, application, and slot each occupy a fixed, distinct position, so
 * there is no symmetry to normalize away.
 */
@Embedded
public class MongoProfileSlotId implements HexableId {

    public static int SIZE = new ObjectId().toByteArray().length * 2 + Integer.BYTES;

    @Property
    private ObjectId userId;

    @Property
    private ObjectId applicationId;

    @Property
    private int slot;

    public MongoProfileSlotId() {}

    public MongoProfileSlotId(final String hex) {
        this(Hex.decode(hex));
    }

    public MongoProfileSlotId(final byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public MongoProfileSlotId(final ByteBuffer byteBuffer) {
        try {
            userId = new ObjectId(byteBuffer);
            applicationId = new ObjectId(byteBuffer);
            slot = byteBuffer.getInt();
        } catch (BufferUnderflowException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public MongoProfileSlotId(final ObjectId userId, final ObjectId applicationId, final int slot) {
        this.userId = userId;
        this.applicationId = applicationId;
        this.slot = slot;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public String toHexString() {
        final var buffer = ByteBuffer.allocate(SIZE);
        userId.putToByteBuffer(buffer);
        applicationId.putToByteBuffer(buffer);
        buffer.putInt(slot);
        return Hex.encode(buffer.flip());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoProfileSlotId that = (MongoProfileSlotId) o;
        return getSlot() == that.getSlot()
                && Objects.equals(getUserId(), that.getUserId())
                && Objects.equals(getApplicationId(), that.getApplicationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getApplicationId(), getSlot());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoProfileSlotId{");
        sb.append("userId=").append(userId);
        sb.append(", applicationId=").append(applicationId);
        sb.append(", slot=").append(slot);
        sb.append(", (").append(toHexString()).append(")");
        sb.append('}');
        return sb.toString();
    }

}
