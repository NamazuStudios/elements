package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

import java.nio.charset.Charset;
import java.util.UUID;

import static java.util.UUID.nameUUIDFromBytes;

public class RoutingHeader extends Struct {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Represents the status of the routed message.
     */
    public final Enum32<Status> status = new Enum32<>(Status.values());

    public final UTF8String tcpAddress = new UTF8String(128);

    /**
     * Represents the message's final inprocIdentifier.  Typically corresponds to an ID generated from a Node id.
     */
    public final PackedUUID inprocIdentifier = inner(new PackedUUID());

    public enum Status {

        /**
         * Indicates that the routing should continue.
         */
        CONTINUE,

        /**
         * Indicates that the routing is dead and should not.
         */
        DEAD

    }

}
