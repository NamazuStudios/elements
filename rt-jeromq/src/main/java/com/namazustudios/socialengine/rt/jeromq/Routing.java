package com.namazustudios.socialengine.rt.jeromq;

import java.nio.charset.Charset;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.nameUUIDFromBytes;

/**
 * Used to generate various URLs for internal routing of messages.
 */
public class Routing {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Gets a route {@link UUID} for the supplied id.  The expected id corresponds to the unique id of the node This is
     * used to abbreviate potentially long unique IDs over the network and guarantee a fixed-length identifier for the
     * route.
     *
     * @param destinationNodeId the node's ID
     * @return a {@link UUID} based on the application id
     */
    public UUID getDestinationId(final String destinationNodeId) {
        return nameUUIDFromBytes(destinationNodeId.getBytes(UTF_8));
    }

    /**
     * Derives a URL based address for the internal routing of requests for a particular node with the
     * associated route {@link UUID}. This is defined as the inproc://multiplex-[destinationId].
     *
     * Returns the {@link String} representing the internal route address.
     **/
    public String getMultiplexedForDestinationId(final UUID destinationId) {
        return format("inproc://multiplex-%s", destinationId);
    }

    /**
     * Derives a URL based address for the internal routing of requests for a particular node with the
     * associated route {@link UUID}. This is defined as the inproc://multiplex-[destinationId].
     *
     * Returns the {@link String} representing the internal route address.
     **/
    public String getDemultiplexedForDestinationId(final UUID destinationId) {
        return format("inproc://demultiplex-%s", destinationId);
    }

}
