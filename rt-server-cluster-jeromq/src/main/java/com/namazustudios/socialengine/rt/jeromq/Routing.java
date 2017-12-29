package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.remote.MalformedMessageException;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.nameUUIDFromBytes;

/**
 * Used to generate various URLs for internal routing of messages.
 */
public class Routing {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private Identity identity;

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

    /**
     * Provided the {@Link ZMsg}, this will strip the {@link RoutingHeader} from the message and return it.
     *
     * @param msg the message from which to strip routing information
     * @return the {@link RoutingHeader} contained in the message
     */
    public RoutingHeader stripRoutingHeader(final ZMsg msg) {

        final ZMsg identity = getIdentity().popIdentity(msg);

        if (msg.isEmpty()) {
            throw new MalformedMessageException("Message missing delimiter frame or no data follows delimiter.");
        }

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.getByteBuffer().put(msg.pop().getData());
        getIdentity().pushIdentity(msg, identity);

        return routingHeader;

    }

    /**
     * Provided the {@link ZMsg}, this will insert the {@link RoutingHeader} after the identity and before the message
     * contents.
     *
     * @param msg the message to receive the routing information
     * @param routingHeader the routing information to insert
     */
    public void insertRoutingHeader(final ZMsg msg, final RoutingHeader routingHeader) {

        final ZMsg identity = getIdentity().popIdentity(msg);

        final byte[] routingHeaderBytes = new byte[routingHeader.size()];
        routingHeader.getByteBuffer().get(routingHeaderBytes);

        msg.push(routingHeaderBytes);
        getIdentity().pushIdentity(msg, identity);

    }

    public Identity getIdentity() {
        return identity;
    }

    @Inject
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

}
