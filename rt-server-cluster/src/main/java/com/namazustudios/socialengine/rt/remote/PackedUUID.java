package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

import java.util.UUID;

import static java.util.UUID.*;

public class PackedUUID extends Struct {

    public final Signed64 lower = new Signed64();

    public final Signed64 upper = new Signed64();

    /**
     * Generates a random {@link UUID} and stores it to this instance.
     */
    public PackedUUID setRandom() {
        set(randomUUID());
        return this;
    }

    /**
     * Sets the value using the supplied {@link UUID}.
     *
     * @param uuid the {@link UUID}
     */
    public void set(final UUID uuid) {
        lower.set(uuid.getLeastSignificantBits());
        upper.set(uuid.getMostSignificantBits());
    }

    /**
     * Reads in the struct as a {@link UUID}.
     *
     * @return the {@link UUID}
     */
    public UUID get() {
        return new UUID(upper.get(), lower.get());
    }

    @Override
    public int hashCode() {
        return getByteBuffer().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getByteBuffer().equals(obj);
    }

}
