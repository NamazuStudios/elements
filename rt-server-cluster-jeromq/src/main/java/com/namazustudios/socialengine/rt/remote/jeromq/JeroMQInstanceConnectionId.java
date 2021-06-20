package com.namazustudios.socialengine.rt.remote.jeromq;

import java.util.Objects;

import static java.lang.String.format;

public class JeroMQInstanceConnectionId implements Comparable<JeroMQInstanceConnectionId> {

    private static final String CONNECTION_DELIMITER = "#";

    private final String address;

    private final String identifier;

    private final String instanceConnectionAddress;

    public JeroMQInstanceConnectionId(final String instanceConnectionAddress) {
        final var tokens = instanceConnectionAddress.split(CONNECTION_DELIMITER);
        this.address = tokens[0];
        this.identifier = tokens.length > 1 ? tokens[1] : "";
        this.instanceConnectionAddress = this.identifier.isEmpty() ?
            this.address :
            format("%s%s%s", instanceConnectionAddress, CONNECTION_DELIMITER, identifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JeroMQInstanceConnectionId that = (JeroMQInstanceConnectionId) o;
        return Objects.equals(address, that.address) && Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, identifier);
    }

    @Override
    public String toString() {
        return instanceConnectionAddress;
    }

    @Override
    public int compareTo(final JeroMQInstanceConnectionId o) {
        return instanceConnectionAddress.compareTo(o.instanceConnectionAddress);
    }

}
