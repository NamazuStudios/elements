package com.namazustudios.socialengine.dao.mongo.model.savedata;

import com.namazustudios.socialengine.rt.util.Hex;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

@Embedded
public class MongoSaveDataDocumentId {

    public static int SIZE = Integer.BYTES + new ObjectId().toByteArray().length;

    @Property
    private final int slot;

    @Property
    private final ObjectId owner;

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

    public MongoSaveDataDocumentId(final int slot, final ObjectId owner) {
        this.slot = slot;
        this.owner = owner;
    }

    public int getSlot() {
        return slot;
    }

    public ObjectId getOwner() {
        return owner;
    }

    public String toHexString() {
        var buffer = ByteBuffer.allocate(SIZE);
        owner.putToByteBuffer(buffer);
        buffer.putInt(slot);
        return Hex.encode(buffer.flip());
    }

}
