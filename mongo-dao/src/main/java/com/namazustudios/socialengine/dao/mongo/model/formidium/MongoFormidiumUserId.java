package com.namazustudios.socialengine.dao.mongo.model.formidium;

import com.namazustudios.socialengine.dao.mongo.HexableId;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.rt.util.Hex;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@Embedded
public class MongoFormidiumUserId implements HexableId {

    public static final int VERSION = 0;

    @Reference
    private final ObjectId userId;

    @Property
    private final String formidiumInvestorId;

    public MongoFormidiumUserId(final String hexString) {
        try {

            final var binary = Hex.decodeToBuffer(hexString);
            final var version = binary.getInt();

            if (version != VERSION)
                throw new IllegalArgumentException("Version mismatch.");

            userId = new ObjectId(binary);
            formidiumInvestorId = UTF_8.decode(binary).toString();

        } catch (BufferUnderflowException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public MongoFormidiumUserId(final ObjectId userId, final String formidiumInvestorId) {
        this.userId = userId;
        this.formidiumInvestorId = formidiumInvestorId;
    }

    @Override
    public String toHexString() {

        final var objectIdBytes = userId.toByteArray();
        final var formidiumUserIdBytes = formidiumInvestorId.getBytes(UTF_8);

        final var total =
                Integer.BYTES +
                objectIdBytes.length +
                formidiumUserIdBytes.length;

        final var binary = ByteBuffer.allocate(total);

        binary.putInt(VERSION);
        binary.put(objectIdBytes);
        binary.put(formidiumUserIdBytes);
        binary.rewind();

        return Hex.encode(binary);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoFormidiumUserId that = (MongoFormidiumUserId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(formidiumInvestorId, that.formidiumInvestorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, formidiumInvestorId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoFormidiumUserId{");
        sb.append("userId=").append(userId);
        sb.append(", formidiumUserId='").append(formidiumInvestorId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
