package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.AsyncConnectionPool;
import dev.getelements.elements.rt.AsyncConnectionService;
import dev.getelements.elements.rt.Connection;
import dev.getelements.elements.rt.exception.BaseException;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.AsyncControlClient;
import dev.getelements.elements.rt.remote.InstanceConnectionService.InstanceBinding;
import dev.getelements.elements.rt.remote.InstanceStatus;
import com.sun.nio.sctp.PeerAddressChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import zmq.ZError;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static dev.getelements.elements.rt.AsyncConnection.Event.*;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQControlClient.trace;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQRoutingCommand.*;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static javax.security.auth.callback.ConfirmationCallback.OK;
import static zmq.ZError.EAGAIN;

public class JeroMQAsyncControlClient implements AsyncControlClient {

    private static final String POOL_NAME = JeroMQControlClient.class.getSimpleName();

    private static final int DEFAULT_MIN_CONNECTIONS = 1;

    private static final int DEFAULT_MAX_CONNECTIONS = 100;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncControlClient.class);

    private final String instanceConnectAddress;

    private final JeroMQSecurityChain jeroMQSecurityChain;

    private final AsyncConnectionPool<ZContext, ZMQ.Socket> pool;

    public JeroMQAsyncControlClient(final AsyncConnectionService<ZContext, ZMQ.Socket> service,
                                    final String instanceConnectAddress,
                                    final JeroMQSecurityChain securityChain) {
        this(service, instanceConnectAddress, securityChain, DEFAULT_MIN_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
    }

    public JeroMQAsyncControlClient(final AsyncConnectionService<ZContext, ZMQ.Socket> service,
                                    final String instanceConnectAddress,
                                    final JeroMQSecurityChain jeroMQSecurityChain,
                                    final int minConnections,
                                    final int maxConnections) {

        this.instanceConnectAddress = instanceConnectAddress;
        this.jeroMQSecurityChain = jeroMQSecurityChain;

        pool = service.allocatePool(POOL_NAME, minConnections, maxConnections, zContext -> {
            final var socket = JeroMQControlClient.open(jeroMQSecurityChain, zContext);
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
    public Request closeRoutesViaInstance(final InstanceId instanceId,
                                          final String instanceConnectAddress,
                                          final ResponseConsumer<Void> responseConsumer) {

        logger.debug("Closing all routes for instance {}", instanceId);

        return dispatch(() -> {
                final var request = new ZMsg();
                CLOSE_ROUTES_VIA_INSTANCE.pushCommand(request);
                request.add(instanceId.asBytes());
                request.add(instanceConnectAddress.getBytes(CHARSET));
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
                    return new JeroMQInstanceBinding(
                            _c.context(),
                            nodeId,
                            instanceConnectAddress,
                            jeroMQSecurityChain,
                            bindAddress);
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
            ? new Throwable().getStackTrace()
            : new StackTraceElement[]{};

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

                    trace(request, stack);

                    if (JeroMQControlClient.send(request, _c.socket())) {
                        _c.setEvents(READ, ERROR);
                    }

                } else {
                    _c.recycle();
                }
            });

        });

        return () -> pending.set(false);

    }

    private static InternalException remap(final StackTraceElement[] stack, final Exception cause) {
        return remap(stack, () -> new InternalException(cause));
    }

    private static RuntimeException remap(final StackTraceElement[] stack, final JeroMQControlException cause) {
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

    private static BaseException remap(final StackTraceElement[] stack, final BaseException cause) {
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

    private static <T extends RuntimeException> T remap(final StackTraceElement[] stack,
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
