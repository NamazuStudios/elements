package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.ControlClient;
import dev.getelements.elements.rt.remote.InstanceConnectionService;
import dev.getelements.elements.rt.remote.InstanceStatus;
import dev.getelements.elements.rt.remote.RoutingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static dev.getelements.elements.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQCommandServer.TRACE_DELIMITER;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQControlResponseCode.PROTOCOL_ERROR;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQControlResponseCode.SOCKET_ERROR;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQRoutingCommand.*;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zeromq.SocketType.DEALER;

/**
 * Implements the control protocol for interfacing with the {@link JeroMQInstanceConnectionService}.  This client
 * is responsible for performing control operations such as registering a node, deregistering a node, and querying
 * available nodes.
 *
 * This class is designed to be used by a single thread and destroyed.
 */
public class JeroMQControlClient implements ControlClient {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQControlClient.class);

    public static final long DEFAULT_TIMEOUT = 30;

    public static final TimeUnit DEFAULT_TIMEOUT_UNITS = SECONDS;

    private final ZContext zContextShadow;

    private final Supplier<ZContext> zContextShadowSupplier;

    private final Socket socket;

    private final String instanceConnectAddress;

    /**
     * Creates a new isntance of the {@link JeroMQControlClient}.
     *
     * @param zContextShadowSupplier a {@link Supplier} which supplies a shadow {@link ZContext}
     * @param instanceConnectAddress the instance connection address.
     */
    public JeroMQControlClient(final Supplier<ZContext> zContextShadowSupplier,
                               final String instanceConnectAddress) {
        this(zContextShadowSupplier, instanceConnectAddress, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNITS);
    }

    /**
     * Creates a {@link JeroMQControlClient} connecting to the remote instance to perform basic discover and control
     * operations.  This uses a {@link Supplier}  to make a shadow copy of the {@link ZContext} and then closes it
     * when this instance's {@link JeroMQControlClient#close()} method is invoked.
     *
     * Because this instance makes a shadow in the constructor, it can only be used in the same thread that created
     * the instance.
     *
     * @param zContextShadowSupplier a {@link Supplier} which supplies a shadow {@link ZContext}
     * @param instanceConnectAddress 
     */
    public JeroMQControlClient(final Supplier<ZContext> zContextShadowSupplier,
                               final String instanceConnectAddress,
                               final long timeout, final TimeUnit timeUnit) {
        this.zContextShadow = zContextShadowSupplier.get();
        this.zContextShadowSupplier = zContextShadowSupplier;
        this.socket = open(this.zContextShadow);
        this.socket.connect(instanceConnectAddress);
        this.instanceConnectAddress = instanceConnectAddress;
        setReceiveTimeout(timeout, timeUnit);
    }

    /**
     * Opens and configures the {@link Socket} type used to connect to the remote {@link JeroMQCommandServer}.
     *
     * @param zContext the context which to use when creating the socket
     * @return the {@link Socket} type
     */
    public static Socket open(final ZContext zContext) {
        return zContext.createSocket(DEALER);
    }

    @Override
    public RoutingStatus getRoutingStatus() {

        logger.debug("Getting routing status.");

        final ZMsg request = new ZMsg();
        GET_ROUTING_STATUS.pushCommand(request);
        send(request);

        final ZMsg response = recv();
        return new JeroMQRoutingStatus(response);

    }

    @Override
    public InstanceStatus getInstanceStatus() {

        logger.debug("Getting instance status.");

        final ZMsg request = new ZMsg();
        GET_INSTANCE_STATUS.pushCommand(request);
        send(request);

        final ZMsg response = recv();
        return new JeroMQInstanceStatus(response);

    }

    @Override
    public String openRouteToNode(final NodeId nodeId, final String instanceInvokerAddress) {

        logger.debug("Opening route to node {} at {}", nodeId, instanceInvokerAddress);

        final ZMsg request = new ZMsg();
        OPEN_ROUTE_TO_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        request.add(instanceInvokerAddress.getBytes(CHARSET));
        send(request);

        final ZMsg response = recv();
        return response.getFirst().getString(CHARSET);

    }

    @Override
    public void closeRoutesViaInstance(final InstanceId instanceId, final String instanceConnectAddress) {

        logger.debug("Closing all routes for instance {}", instanceId);

        final ZMsg request = new ZMsg();
        CLOSE_ROUTES_VIA_INSTANCE.pushCommand(request);
        request.add(instanceId.asBytes());
        request.add(instanceConnectAddress.getBytes(CHARSET));
        send(request);
        recv();

    }

    @Override
    public InstanceConnectionService.InstanceBinding openBinding(final NodeId nodeId) {

        logger.debug("Opening binding for {}", nodeId);

        final ZMsg request = new ZMsg();

        OPEN_BINDING_FOR_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        send(request);

        final ZMsg response = recv();
        final String nodeBindAddress = response.removeFirst().getString(CHARSET);
        return new JeroMQInstanceBinding(zContextShadowSupplier, nodeId, instanceConnectAddress, nodeBindAddress);

    }

    @Override
    public void closeBinding(final NodeId nodeId) {

        logger.debug("Closing binding for {}", nodeId);

        final ZMsg request = new ZMsg();
        CLOSE_BINDING_FOR_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        send(request);
        recv();

    }

    @Override
    public void setReceiveTimeout(long timeout, TimeUnit timeUnit) {
        final var to = (int) MILLISECONDS.convert(timeout, timeUnit);
        this.socket.setReceiveTimeOut(to);
    }

    private void send(final ZMsg zMsg) {
        trace(zMsg);
        send(zMsg, socket);
    }

    private void trace(final ZMsg zMsg) {
        if (logger.isDebugEnabled()) {
            trace(zMsg, new Throwable().fillInStackTrace().getStackTrace());
        }
    }

    /**
     * Sends the supplied {@link ZMsg} on the supplied {@link Socket}, adding header and delimiter
     * information as necessary
     *
     * @param zMsg the {@link ZMsg} to send
     * @param socket the {@link Socket} to use to send the request
     */
    public static boolean send(final ZMsg zMsg, final Socket socket) {
        zMsg.addFirst(EMPTY_DELIMITER);
        return zMsg.send(socket);
    }

    /**
     * Embeds the supplied stack trace in the supplied {@link ZMsg}. The remote end will ignore the trace, however it
     * will be visible in the debugger so it is possible to know where in the client code the message originated.
     *
     * @param trace the trace
     * @param zMsg the message
     */
    public static void trace(final ZMsg zMsg, final StackTraceElement[] trace) {
        if (trace != null && trace.length > 0) {
            zMsg.addLast(TRACE_DELIMITER.getBytes(CHARSET));
            for (var element : trace) zMsg.addLast(element.toString());
        }
    }

    private ZMsg recv() {
        return recv(socket);
    }

    /**
     * Receives a message from the supplied {@link Socket} instance.
     *
     * @param socket the socket on which to receive the incoming message.
     *
     * @return the {@link ZMsg} instance
     */
    public static ZMsg recv(final Socket socket) {
        final var response = ZMsg.recvMsg(socket);
        return check(response);
    }

    private static ZMsg check(final ZMsg response) {

        if (response == null) throw new JeroMQControlException(SOCKET_ERROR);
        if (response.isEmpty()) throw new JeroMQControlException(PROTOCOL_ERROR);

        final ZFrame delimiter = response.removeFirst();
        if (delimiter.getData().length != 0) throw new JeroMQControlException(PROTOCOL_ERROR);

        JeroMQControlResponseCode code;

        try {
            code = JeroMQControlResponseCode.stripCode(response);
        } catch (IllegalArgumentException ex) {
            code = JeroMQControlResponseCode.UNKNOWN_ERROR;
        }

        switch (code) {
            case OK: return response;
            case NO_SUCH_NODE_ROUTE: throw new JeroMQUnroutableNodeException(response);
            default: throw new JeroMQControlException(code, response);
        }

    }

    @Override
    public void close() {
        socket.close();
        zContextShadow.close();
    }

}
