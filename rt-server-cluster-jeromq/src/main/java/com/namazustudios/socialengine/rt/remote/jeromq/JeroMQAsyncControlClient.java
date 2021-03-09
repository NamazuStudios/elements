package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.Connection;
import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.AsyncControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import com.namazustudios.socialengine.rt.remote.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.AsyncConnection.Event.*;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingCommand.*;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;

public class JeroMQAsyncControlClient implements AsyncControlClient {

    private static final String POOL_NAME = JeroMQControlClient.class.getSimpleName();

    private static final int DEFAULT_MIN_CONNECTIONS = 1;

    private static final int DEFAULT_MAX_CONNECTIONS = 100;

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
            final var socket = JeroMQControlClient.open(zContext);
            socket.connect(instanceConnectAddress);
            return socket;
        });

    }

    @Override
    public Request getInstanceStatus(final ResponseConsumer<InstanceStatus> responseConsumer) {

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
                                   final ResponseConsumer<String> responseConsumer) {

        logger.debug("Opening route to node {} at {}", nodeId, instanceInvokerAddress);

        return dispatch(() -> {
                final ZMsg request = new ZMsg();
                OPEN_ROUTE_TO_NODE.pushCommand(request);
                request.add(nodeId.asBytes());
                request.add(instanceInvokerAddress.getBytes(CHARSET));
                return request;
            },
            zMsgResponse -> {
                final var response = zMsgResponse.map(zMsg -> zMsg.getFirst().getString(CHARSET));
                responseConsumer.accept(response);
            }
        );

    }

    @Override
    public Request closeRouteToNode(final NodeId nodeId,
                                    final ResponseConsumer<Void> responseConsumer) {

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
    public Request closeRoutesViaInstance(InstanceId instanceId, ResponseConsumer<Void> responseConsumer) {

        logger.debug("Closing all routes for instance {}", instanceId);

        return dispatch(() -> {
                final var request = new ZMsg();
                CLOSE_ROUTES_VIA_INSTANCE.pushCommand(request);
                request.add(instanceId.asBytes());
                return request;
            },
            zMsgResponse -> {
                final var response = zMsgResponse.map(zMsg -> (Void) null);
                responseConsumer.accept(response);
            }
        );

    }

    @Override
    public Request openBinding(final NodeId nodeId, final ResponseConsumer<InstanceBinding> responseConsumer) {

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
                                final ResponseConsumer<Void> responseConsumer) {

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
                                 final ResponseConsumer<ZMsg> responseConsumer) {
        return dispatch(outgoingSupplier, (_c, zMsgResponse) -> responseConsumer.accept(zMsgResponse));
    }

    private <T> Request dispatch(
            final Supplier<ZMsg> outgoingSupplier,
            final BiConsumer<Connection<ZContext, ZMQ.Socket>, Response<ZMsg>> responseConsumer) {

        final var pending = new AtomicBoolean(true);

        final var stack = logger.isDebugEnabled()
            ? new Throwable().getStackTrace() :
            new StackTraceElement[]{};

        pool.acquireNextAvailableConnection(c -> {

            c.setEvents(WRITE, ERROR, READ);

            c.onError(_c -> {
                try {
                    if (pending.compareAndSet(true, false)) {

                        final int errno = _c.socket().errno();

                        responseConsumer.accept(_c, () -> {
                            throw remap(stack, () -> new InternalException("ZMQ Errno: " + errno));
                        });

                    }
                } finally {
                    _c.close();
                }
            });

            c.onClose(_c -> {
                if (pending.compareAndSet(true, false)) {
                    responseConsumer.accept(_c, () -> {
                        throw remap(stack, () -> new InternalException("Connection closed."));
                    });
                }
            });

            c.onRecycle(_c -> {
                if (pending.compareAndSet(true, false)) {
                    responseConsumer.accept(_c, () -> {
                        throw remap(stack, () -> new InternalException("Connection recycled."));
                    });
                }
            });

            c.onRead(_c -> {
                try {
                    if (pending.compareAndSet(true, false)) {
                        try {
                            final ZMsg zMsg = JeroMQControlClient.recv(_c.socket());
                            responseConsumer.accept(_c, () -> zMsg);
                        } catch (BaseException ex) {
                            responseConsumer.accept(_c, () -> { throw remap(stack, ex); });
                        } catch (JeroMQControlException ex) {
                            responseConsumer.accept(_c, () -> { throw remap(stack, ex); });
                        } catch (Exception ex) {
                            responseConsumer.accept(_c, () -> { throw remap(stack, ex); });
                        }
                    } else {
                        logger.info("Dropping message that was canceled.");
                    }
                } finally {
                    _c.recycle();
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

    private InternalException remap(final StackTraceElement[] stack, final Exception cause) {
        return remap(stack, () -> new InternalException(cause));
    }

    private RuntimeException remap(final StackTraceElement[] stack, final JeroMQControlException cause) {
        return remap(stack, () -> {
            try {
                return cause
                    .getClass()
                    .getConstructor(cause.getClass())
                    .newInstance(cause);
            } catch (IllegalAccessException | InstantiationException |
                     InvocationTargetException | NoSuchMethodException e) {
                return remap(stack, () -> new InternalException(e));
            }
        });
    }

    private BaseException remap(final StackTraceElement[] stack, final BaseException cause) {
        return remap(stack, () -> {
            try {
                return cause
                        .getClass()
                        .getConstructor(Throwable.class)
                        .newInstance(cause);
            } catch (IllegalAccessException | InstantiationException |
                    InvocationTargetException | NoSuchMethodException e) {
                return remap(stack, () -> new InternalException(e));
            }
        });
    }

    private <T extends RuntimeException> T remap(final StackTraceElement[] stack,
                                              final Supplier<T> baseExceptionSupplier) {
        final var remapped = baseExceptionSupplier.get();
        remapped.setStackTrace(stack);
        return remapped;
    }

    @Override
    public void close() {
        pool.close();
    }

}
