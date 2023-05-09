package dev.getelements.elements.dao.mongo.model.formidium;

import dev.getelements.elements.dao.mongo.HexableId;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.util.Hex;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

@Embedded
public class MongoFormidiumInvestorId implements HexableId, Comparable<MongoFormidiumInvestorId> {

    public static final byte VERSION = 0;

    public static final int HASH_BYTE_COUNT = 256/8;

    public static final String HASH_ALGORITHM = "SHA-256";

    @Property
    private String hash;

    public static Optional<MongoFormidiumInvestorId> tryParse(final String formidiumInvestorId) {
        try {
            final var id = new MongoFormidiumInvestorId(formidiumInvestorId);
            return Optional.of(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public MongoFormidiumInvestorId() {}

    public MongoFormidiumInvestorId(final String hexString) {

        // Called via reflection to do basic data integrity checks.

        try {

            final var binary = Hex.decodeToBuffer(hexString).rewind();
            final var version = binary.get();

            if (version != VERSION)
                throw new IllegalArgumentException("Version mismatch.");

            if (binary.remaining() != HASH_BYTE_COUNT)
                throw new IllegalArgumentException("Incorrect ID length.");

            hash = Hex.encode(binary);

        } catch (BufferUnderflowException ex) {
            throw new IllegalArgumentException(ex);
        }

    }

    public MongoFormidiumInvestorId(final ObjectId userId) {
        try {
            final var digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(userId.toByteArray());
            hash = Hex.encode(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            // Should never happen if the VM properly implements the specification as SHA
            throw new InternalException(ex);
        }
    }

    @Override
    public int compareTo(final MongoFormidiumInvestorId o) {
        return this.hash.compareTo(o.hash);
    }

    @Override
    public String toHexString() {

        final var total = 1 + HASH_BYTE_COUNT;
        final var binary = ByteBuffer.allocate(total);

        binary.put(VERSION);
        binary.put(Hex.decode(hash));
        binary.rewind();

        return Hex.encode(binary);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoFormidiumInvestorId that = (MongoFormidiumInvestorId) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoFormidiumInvestorId{");
        sb.append("hash='").append(hash).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
