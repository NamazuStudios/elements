package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.Connection;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.AsyncControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingCommand.*;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static org.zeromq.SocketType.REQ;

public class JeroMQAsyncControlClient implements AsyncControlClient {

    private static final String POOL_NAME = JeroMQControlClient.class.getSimpleName();

    private static final int DEFAULT_MIN_CONNECTIONS = 1;

    private static final int DEFAULT_MAX_CONNECTIONS = 10;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncControlClient.class);

    private final String instanceConnectAddress;

    private final AsyncConnectionPool<ZContext, ZMQ.Socket> pool;

    public JeroMQAsyncControlClient(final AsyncConnectionService<ZContext, ZMQ.Socket> service,
                                    final String instanceConnectAddress) {
        this(service, instanceConnectAddress, DEFAULT_MIN_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
    }

    public JeroMQAsyncControlClient(final AsyncConnectionService<ZContext, ZMQ.Socket> service,
                                    final String instanceConnectAddress,
                                    final int minConnections,
                                    final int maxConnections) {

        this.instanceConnectAddress = instanceConnectAddress;

        pool = service.allocatePool(POOL_NAME, minConnections, maxConnections, zContext -> {
            final var socket = zContext.createSocket(REQ);
            socket.connect(instanceConnectAddress);
            return socket;
        });

    }

    @Override
    public Request getInstanceStatus(final Consumer<Response<? extends InstanceStatus>> responseConsumer) {

        logger.debug("Getting instance status.");

        return dispatch(() -> {
                final ZMsg request = new ZMsg();
                GET_INSTANCE_STATUS.pushCommand(request);
                return request;
            },
            zMsgResponse -> {
                final var response = zMsgResponse.map(JeroMQInstanceStatus::new);
                responseConsumer.accept(response);
            }
        );

    }

    @Override
    public Request openRouteToNode(final NodeId nodeId, final String instanceInvokerAddress,
                                   final Consumer<Response<String>> responseConsumer) {

        logger.debug("Opening route to node {} at {}", nodeId, instanceInvokerAddress);

        return dispatch(() -> {
                final var request = new ZMsg();
                GET_INSTANCE_STATUS.pushCommand(request);
                return request;
            },
            zMsgResponse -> {
                final var response = zMsgResponse.map(zMsg -> zMsg.getFirst().getString(CHARSET));
                responseConsumer.accept(response);
            }
        );

    }

    @Override
    public Request closeRouteToNode(NodeId nodeId, Consumer<Response<Void>> responseConsumer) {

        logger.debug("Closing route to node {}", nodeId);

        return dispatch(() -> {
                final var request = new ZMsg();
                CLOSE_ROUTE_TO_NODE.pushCommand(request);
                request.add(nodeId.asBytes());
                return request;
            },
            zMsgResponse -> responseConsumer.accept(zMsgResponse.map(zMsg -> null))
        );

    }

    @Override
    public Request closeRoutesViaInstance(InstanceId instanceId, Consumer<Response<Void>> responseConsumer) {

        logger.debug("Closing all routes for instance {}", instanceId);

        return dispatch(() -> {
                final var request = new ZMsg();
                CLOSE_ROUTES_VIA_INSTANCE.pushCommand(request);
                request.add(instanceId.asBytes());
                return request;
            },
            zMsgResponse -> responseConsumer.accept(zMsgResponse.map(zMsg -> null))
        );

    }

    @Override
    public Request openBinding(
            final NodeId nodeId,
            final Consumer<Response<? extends InstanceConnectionService.InstanceBinding>> responseConsumer) {

        logger.debug("Opening binding for {}", nodeId);

        return dispatch(() -> {
                final var request = new ZMsg();
                OPEN_BINDING_FOR_NODE.pushCommand(request);
                request.add(nodeId.asBytes());
                return request;
            },
            (_c, zMsgResponse) -> {

                final var response = zMsgResponse.map(zMsg -> {
                    final var bindAddress = zMsg.remove().getString(CHARSET);
                    return new JeroMQInstanceBinding(_c.context(), nodeId, instanceConnectAddress, bindAddress);
                });

                responseConsumer.accept(response);

            }
        );

    }

    @Override
    public Request closeBinding(final NodeId nodeId,
                                final Consumer<Response<Void>> responseConsumer) {

        logger.debug("Closing binding for {}", nodeId);

        return dispatch(() -> {
                final var request = new ZMsg();
                CLOSE_BINDING_FOR_NODE.pushCommand(request);
                request.add(nodeId.asBytes());
                return request;
            },
            zMsgResponse -> responseConsumer.accept(zMsgResponse.map(zMsg -> null))
        );

    }

    private <T> Request dispatch(final Supplier<ZMsg> outgoingSupplier,
                                 final Consumer<Response<ZMsg>> responseConsumer) {
        return dispatch(outgoingSupplier, (_c, zMsgResponse) -> responseConsumer.accept(zMsgResponse));
    }

    private <T> Request dispatch(final Supplier<ZMsg> outgoingSupplier,
                             final BiConsumer<Connection<ZContext, ZMQ.Socket>, Response<ZMsg>> responseConsumer) {

        final var pending = new AtomicBoolean(true);

        pool.acquireNextAvailableConnection(c -> {

            c.onError(_c -> {

                if (pending.compareAndSet(true, false)) {

                    final int errno = _c.socket().errno();

                    responseConsumer.accept(_c, () -> {
                        throw new InternalException("ZMQ Errno: " + errno);
                    });

                }

                _c.close();

            });

            c.onClose(_c -> {
                if (pending.compareAndSet(true, false)) {
                    responseConsumer.accept(_c, () -> { throw new InternalException("Client closed."); });
                }
            });

            c.onRead(_c -> {
                if (pending.compareAndSet(true, false)) {
                    responseConsumer.accept(_c, () -> JeroMQControlClient.recv(_c.socket()));
                    _c.recycle();
                } else {
                    logger.info("Dropping message that was canceled.");
                }
            });

            c.onWrite(_c -> {
                if (pending.get()) {
                    final var request = outgoingSupplier.get();
                    JeroMQControlClient.send(request, _c.socket());
                } else {
                    _c.recycle();
                }
            });

        });

        return () -> pending.set(false);

    }

    @Override
    public void close() {

    }

}
