package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.MalformedMessageException;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import org.zeromq.ZMsg;

import java.nio.charset.Charset;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.nameUUIDFromBytes;

/**
 * Used to generate various URL strings for internal routing of messages.
 */
public class RouteRepresentationUtil {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String buildTcpAddress(final String host, final int port) {
        if (host == null || host.length() == 0 || port < 0) {
            return null;
        }

        final String validatedHost;

        if (host.endsWith(".")) {
            validatedHost = host.substring(0, host.length() - 1);
        }
        else {
            validatedHost = host;
        }

        final String backendAddress = "tcp://" + validatedHost + ":" + port;

        return backendAddress;
    }

    public static boolean isHostLocalhost(final String host) {
        final String validatedHost;

        if (host.endsWith(".")) {
            validatedHost = host.substring(0, host.length() - 1);
        }
        else {
            validatedHost = host;
        }

        // TODO: should get this list validated
        if (validatedHost.equals("localhost")
                || validatedHost.equals("*")
                || validatedHost.equals("::1")
                || validatedHost.equals("127.0.0.1")
                || validatedHost.equals("0.0.0.0")
        ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Gets a route {@link UUID} for the supplied id string representation.  The expected id corresponds to the unique
     * id of the node This is used to abbreviate potentially long unique IDs over the network and guarantee a
     * fixed-length identifier for the route.
     *
     * @param inprocIdentifierString the node's ID
     * @return a {@link UUID} based on the application id
     */
    public static UUID buildInprocIdentifierFromString(final String inprocIdentifierString) {
        return nameUUIDFromBytes(inprocIdentifierString.getBytes(UTF_8));
    }

    /**
     * Derives a URL based address for the internal routing of requests for a particular node with the
     * associated route {@link UUID}. This is defined as the inproc://multiplex-[destinationId].
     *
     * Returns the {@link String} representing the internal route address.
     **/
    public static String buildMultiplexInprocAddress(final NodeId nodeId) {
        if (nodeId.getApplicationUuid() != null) {
            return format("inproc://multiplex-%s.%s", nodeId.getInstanceId(), nodeId.getApplicationUuid());
        } else {
            return format("inproc://multiplex-%s", nodeId.getInstanceId());
        }
    }

    /**
     * Derives a URL based address for the internal routing of requests for a particular node with the
     * associated route {@link UUID}. This is defined as the inproc://demultiplex-[destinationId].
     *
     * Returns the {@link String} representing the internal route address.
     **/
    public static String buildDemultiplexInprocAddress(final NodeId nodeId) {
        if (nodeId.getApplicationUuid() != null) {
            return format("inproc://demultiplex-%s.%s", nodeId.getInstanceId(), nodeId.getApplicationUuid());
        }
        else {
            return format("inproc://demultiplex-%s", nodeId.getInstanceId());
        }
    }

    /**
     * Provided the {@Link ZMsg}, this will strip the {@link RoutingHeader} from the message and return it.
     *
     * @param msg the message from which to strip routing information
     * @return the {@link RoutingHeader} contained in the message
     */
    public static RoutingHeader getAndStripRoutingHeader(final ZMsg msg) {

        final ZMsg identity = IdentityUtil.popIdentity(msg);

        if (msg.isEmpty()) {
            throw new MalformedMessageException("Message missing delimiter frame or no data follows delimiter.");
        }

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.getByteBuffer().put(msg.pop().getData());
        IdentityUtil.pushIdentity(msg, identity);

        return routingHeader;

    }

    /**
     * Provided the {@link ZMsg}, this will insert the {@link RoutingHeader} after the identityUtil and before the message
     * contents.
     *
     * @param msg the message to receive the routing information
     * @param routingHeader the routing information to insert
     */
    public static void insertRoutingHeader(final ZMsg msg, final RoutingHeader routingHeader) {

        final ZMsg identity = IdentityUtil.popIdentity(msg);

        final byte[] routingHeaderBytes = new byte[routingHeader.size()];
        routingHeader.getByteBuffer().get(routingHeaderBytes);

        msg.push(routingHeaderBytes);
        IdentityUtil.pushIdentity(msg, identity);

    }

}
