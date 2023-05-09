package dev.getelements.elements.dao.mongo.model.savedata;

import dev.getelements.elements.dao.mongo.HexableId;
import dev.getelements.elements.rt.util.Hex;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

@Embedded
public class MongoSaveDataDocumentId implements HexableId {

    public static int SIZE = Integer.BYTES + new ObjectId().toByteArray().length;

    @Property
    private int slot;

    @Property
    private ObjectId owner;

    public MongoSaveDataDocumentId() {}

    public MongoSaveDataDocumentId(final String hex) {
        this(Hex.decode(hex));
    }

    public MongoSaveDataDocumentId(final byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public MongoSaveDataDocumentId(final ByteBuffer byteBuffer) {
        try {
            owner = new ObjectId(byteBuffer);
            slot = byteBuffer.getInt();
        } catch (BufferUnderflowException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public MongoSaveDataDocumentId(final ObjectId owner, final int slot) {
        this.slot = slot;
        this.owner = owner;
    }

    public int getSlot() {
        return slot;
    }

    public ObjectId getOwner() {
        return owner;
    }

    @Override
    public String toHexString() {
        var buffer = ByteBuffer.allocate(SIZE);
        owner.putToByteBuffer(buffer);
        buffer.putInt(slot);
        return Hex.encode(buffer.flip());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoSaveDataDocumentId that = (MongoSaveDataDocumentId) o;
        return getSlot() == that.getSlot() && Objects.equals(getOwner(), that.getOwner());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSlot(), getOwner());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoSaveDataDocumentId{");
        sb.append("slot=").append(slot);
        sb.append(", owner=").append(owner);
        sb.append(", (").append(toHexString()).append(")");
        sb.append('}');
        return sb.toString();
    }

}
