package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.AsyncConnection;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.rt.exception.*;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.exception.InvalidInstanceIdException;
import dev.getelements.elements.sdk.cluster.id.exception.InvalidNodeIdException;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import zmq.ZError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import static dev.getelements.elements.rt.AsyncConnection.Event.*;
import static dev.getelements.elements.sdk.cluster.id.NodeId.nodeIdFromBytes;
import static dev.getelements.elements.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQAsyncOperation.State.*;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQControlResponseCode.stripCode;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.zeromq.ZMQ.SNDMORE;

public class JeroMQRemoteInvocation {

    static final String TRACE_LOGGER_NAME = format("%s.trace", JeroMQRemoteInvocation.class.getName());

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvocation.class);

    private static final Logger traceLogger = LoggerFactory.getLogger(TRACE_LOGGER_NAME);

    static final String CALL_ID_MDC = format("%s.uid", JeroMQRemoteInvocation.class.getSimpleName());

    static final String CALL_DETAILS_MDC = format("%s.details", JeroMQRemoteInvocation.class.getSimpleName());

    private static final LongSupplier callIdSupplier;

    private static final Consumer<JeroMQRemoteInvocation> trace;

    private static final Consumer<JeroMQRemoteInvocation> untrace;

    private static long noop() {return 0;}

    private static void noop(Object object) {}

    static {
        if (traceLogger.isTraceEnabled()) {

            final Collection<JeroMQRemoteInvocation> active = newKeySet();

            trace = active::add;
            untrace = active::remove;
            callIdSupplier = new AtomicLong()::incrementAndGet;

            newSingleThreadScheduledExecutor(r -> {
                final var thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }).scheduleAtFixedRate(() -> active.forEach(invocation -> {
                invocation.enterMdc();
                traceLogger.trace("Active {}", invocation.getInformationString());
            }), 0,1, TimeUnit.SECONDS);

        } else {
            trace = JeroMQRemoteInvocation::noop;
            untrace = JeroMQRemoteInvocation::noop;
            callIdSupplier = JeroMQRemoteInvocation::noop;
        }
    }

    private String getInformationString() {

        final var sb = new StringBuilder();
        final var name = invocation.getName();

        if (name != null && !name.isBlank()) {
            sb.append(format("@Named(\"%s\") ", name));
        }

        final Function<Object, String> transformer = object -> {
            if (object instanceof Collection) {
                return "{" +
                    ((Collection<?>) object)
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) +
                "}";
            } else if (object instanceof Object[]) {
                return Arrays.toString((Object[]) object);
            } else if (object == null) {
                return "<null>";
            } else {
                return object.toString();
            }
        };

        sb.append(invocation.getDispatchType()).append(" ")
          .append(invocation.getType()).append(".")
          .append(invocation.getMethod())
          .append("(")
          .append(invocation.getArguments().stream().map(transformer).collect(joining(",")))
          .append(")");

        return sb.toString();

    }

    private static <T> Consumer<T> wrap(final Consumer<T> consumer) {
        return traceLogger.isTraceEnabled() ? o -> {
            try {
                consumer.accept(o);
            } catch (Exception ex) {
                traceLogger.trace("Caught exception in results handler: {}", ex.getMessage());
                throw ex;
            }
        } : consumer;
    }

    private static <T> List<Consumer<T>> wrap(final List<Consumer<T>> consumers) {
        return traceLogger.isTraceEnabled()
            ? consumers.stream().map(JeroMQRemoteInvocation::wrap).collect(toList())
            : consumers;
    }

    private InvocationErrorConsumer wrap(final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return traceLogger.isTraceEnabled() ? o -> {
            try {
                asyncInvocationErrorConsumer.accept(o);
            } catch (Exception ex) {
                traceLogger.trace("Caught exception in handler: {}", ex.getMessage());
                throw ex;
            }
        } : asyncInvocationErrorConsumer;
    }

    private final long callId = callIdSupplier.getAsLong();

    private final JeroMQAsyncOperation asyncOperation;

    private final PayloadReader payloadReader;

    private final PayloadWriter payloadWriter;

    private final Map<String, String > mdcContext;

    private final Consumer<Object> syncResultConsumer;

    private final Consumer<Throwable> syncErrorConsumer;

    private final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList;

    private final InvocationErrorConsumer asyncInvocationErrorConsumer;

    private final Invocation invocation;

    private final int additionalCount;

    private int remaining;

    private boolean syncCompleted;

    private boolean asyncCompleted;

    private final int expectedResponseCount;

    private final Subscription subscriptions;

    public JeroMQRemoteInvocation(final JeroMQAsyncOperation asyncOperation,
                                  final AsyncConnection<ZContext, ZMQ.Socket> connection,
                                  final Invocation invocation,
                                  final PayloadReader payloadReader,
                                  final PayloadWriter payloadWriter,
                                  final Map<String, String > mdcContext,
                                  final Consumer<Object> syncResultConsumer,
                                  final Consumer<Throwable> syncErrorConsumer,
                                  final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                  final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        // Immutable
        this.asyncOperation = asyncOperation;
        this.mdcContext = mdcContext == null ? emptyMap() : mdcContext;
        this.payloadReader = payloadReader;
        this.payloadWriter = payloadWriter;
        this.syncResultConsumer = wrap(syncResultConsumer);
        this.syncErrorConsumer = wrap(syncErrorConsumer);
        this.asyncInvocationResultConsumerList = wrap(asyncInvocationResultConsumerList);
        this.asyncInvocationErrorConsumer = wrap(asyncInvocationErrorConsumer);
        this.expectedResponseCount = asyncInvocationResultConsumerList.size();
        this.invocation = invocation;
        this.additionalCount = asyncInvocationResultConsumerList.size();

        // Mutable
        this.remaining = expectedResponseCount;
        this.asyncCompleted = expectedResponseCount == 0;

        connection.setEvents(READ, WRITE, ERROR);

        subscriptions = Subscription.begin()
            .chain(connection.onRead(this::handleRead))
            .chain(connection.onClose(this::handleClose))
            .chain(connection.onError(this::handleSocketError))
            .chain(connection.onWrite(this::handleWrite));

        trace.accept(this);

        enterMdc();
        traceLogger.trace("Beginning Invocation.");

    }

    private void enterMdc() {
        MDC.setContextMap(mdcContext);
        MDC.put(CALL_ID_MDC, format("%019x", callId));
        MDC.put(CALL_DETAILS_MDC, getInformationString());
    }

    private void handleRead(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

        enterMdc();

        try {

            logger.debug("Received message {}", this);
            traceLogger.trace("Received message.");

            final var msg = recv(connection);
            handleResponse(msg);
            traceLogger.trace("Message received and result handled.");

            if (asyncCompleted && syncCompleted) {

                traceLogger.trace("All operations complete. Requesting finish.");

                final var cs = asyncOperation.requestFinish();
                traceLogger.trace("Requested finish with state: {}", cs);

                if (FINISH_PENDING.equals(cs.getState())) {
                    // We finished with the happy path. The connection is finished and we will simply.
                    // set the state and ignore any potential errors at this point
                    asyncOperation.finish();
                    logger.debug("Finished Invocation.");
                    traceLogger.trace("Finished Invocation.");
                } else {
                    // We did not successfully finish the connection because a cancellation request beat us
                    // to the punch. Therefore we send the cancellation error. However, as the connection has
                    // completed we just drive the error on our end.
                    asyncInvocationErrorConsumer.accept(cs.getInvocationError());
                    logger.debug("Invocation cancelled at finish time.");
                    traceLogger.trace("Invocation cancelled at finish time.");
                }

                // In any case the connection is in a valid state and we can return it to the pool. The remote
                // end did its job and this invocation is finished and can be unsubscribed and returned to the
                // pool without any issues.

                connection.recycle();
                logger.debug("Recycled connection.");
                traceLogger.trace("Recycled connection.");

                subscriptions.unsubscribe();
                logger.debug("Unsubscribed events.");
                traceLogger.trace("Unsubscribed events.");

                untrace.accept(this);
                traceLogger.trace("Stopped tracing connection.");

            }

        } catch (Exception ex) {

            // This is typical of an internal exception (such as a socket error, IO Exception etc.) and should be
            // handed to the clients to ensure that they do not wait around for a response they're never going to
            // get.  So, therefore, the exception is called on both the future task as well as the async handler.
            // For good measure, the exception is re-thrown so the connection pool properly closes the connection
            // as we can't assume that socket is still in a stable state.

            logger.error("Caught error running remote invocation.", ex);
            traceLogger.trace("Response Handler Threw Exception: {}", ex.getMessage());

            final var cs = asyncOperation.requestFinish();

            final var state = cs.getState();
            traceLogger.trace("Invocation Operation is {}. Sending result and finishing.", state);

            if (FINISH_PENDING.equals(state)) {

                // We finished with the happy path. The connection is finished and we will simply.
                // set the state and ignore any potential errors at this point

                traceLogger.trace("Replying with result.");

                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(ex);

                // We drive the exception to both places appropriately.
                traceLogger.trace("Replying with synchronous errors: {}", ex.getMessage());
                syncErrorConsumer.accept(ex);

                traceLogger.trace("Replying with asynchronous errors: {}", ex.getMessage());
                asyncInvocationErrorConsumer.acceptAndLogError(logger, invocationError);

                traceLogger.trace("Finishing pending operation.");
                asyncOperation.finish();

            } else {

                // We did not successfully finish the connection because a cancellation request beat us
                // to the punch. Therefore we send the cancellation error. However, as the connection has
                // completed we just drive the error on our end.

                traceLogger.trace("Replying with synchronous cancellation message.");
                syncErrorConsumer.accept(cs.getError());

                traceLogger.trace("Replying with asynchronous cancellation message.");
                asyncInvocationErrorConsumer.acceptAndLogError(logger, cs.getInvocationError());

            }

            // Cautiously we should nuke this connection because it could be placed into an undefined state.
            subscriptions.unsubscribe();
            logger.debug("Unsubscribed events.");
            traceLogger.trace("Unsubscribed events.");

            connection.close();
            logger.debug("Error. Closing connection.");
            traceLogger.trace("Error. Closing connection.");

            untrace.accept(this);
            traceLogger.trace("Stopped tracing connection.");

        }

    }

    private void handleWrite(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

        enterMdc();
        logger.debug("Got socket write event. Sending invocation.");

        final RequestHeader requestHeader = new RequestHeader();
        requestHeader.additionalParts.set(additionalCount);

        final byte[] payload;

        try {
            payload = payloadWriter.write(invocation);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        connection.socket().send(EMPTY_DELIMITER, SNDMORE);
        connection.socket().sendByteBuffer(requestHeader.getByteBuffer(), SNDMORE);
        connection.socket().send(payload);
        connection.setEvents(READ, ERROR);

    }

    private ZMsg recv(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

        final ZMsg zMsg = ZMsg.recvMsg(connection.socket());
        final int error = connection.socket().errno();

        if (zMsg == null && error == ZError.EAGAIN) {
            throw new HandlerTimeoutException("Remote invocation timed out for addr.");
        } else if (zMsg == null) {
            throw new InternalException("Got null response from socket.");
        }

        zMsg.removeFirst();

        final JeroMQControlResponseCode code = stripCode(zMsg);

        switch (code) {
            case OK:
                return zMsg;
            case NO_SUCH_NODE_ROUTE:
                throw extractNodeNotFoundException(zMsg);
            case NO_SUCH_INSTANCE:
                throw extractInstanceNotFoundException(zMsg);
            default:
                throw extractException(zMsg);
        }

    }

    private RuntimeException extractException(final ZMsg zMsg) {

        final ZFrame msgFrame = zMsg.removeFirst();
        final ZFrame exceptionFrame = zMsg.removeFirst();
        final String message = new String(msgFrame.getData(), CHARSET);

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(exceptionFrame.getData());
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (RuntimeException)ois.readObject();
        } catch (Exception ex) {
            return new InternalException(message, ex);
        }

    }

    private NodeNotFoundException extractNodeNotFoundException(ZMsg zMsg) {

        final Throwable cause = extractException(zMsg);

        try {
            final NodeId nodeId = nodeIdFromBytes(zMsg.removeLast().getData());
            return new NodeNotFoundException(nodeId, cause);
        } catch (InvalidNodeIdException ex) {
            return new NodeNotFoundException(cause);
        }

    }

    private InstanceNotFoundException extractInstanceNotFoundException(final ZMsg zMsg) {

        final Throwable cause = extractException(zMsg);

        try {
            final InstanceId instanceId = new InstanceId(zMsg.removeLast().getData());
            return new InstanceNotFoundException(instanceId, cause);
        } catch (InvalidInstanceIdException ex) {
            return new InstanceNotFoundException(cause);
        }

    }

    private void handleResponse(final ZMsg msg) {

        final ResponseHeader responseHeader = receiveHeader(msg);

        switch (responseHeader.type.get()) {
            case INVOCATION_RESULT:
                handleResult(msg, responseHeader);
                break;
            case INVOCATION_ERROR:
                handleError(msg, responseHeader);
                break;
            default:
                // This should only happen if there is a bug in the code.
                logger.error("Invalid response type {}", responseHeader.type.get());
                traceLogger.trace("Call Error. Unknown type: " + responseHeader.type.get());
                throw new InternalException("Invalid response type " + responseHeader.type.get());
        }

    }

    private ResponseHeader receiveHeader(final ZMsg msg) {
        final ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.getByteBuffer().put(msg.pop().getData());
        return responseHeader;
    }

    private void handleResult(final ZMsg msg, final ResponseHeader responseHeader) {

        logger.debug("Got invocation result.");

        final InvocationResult invocationResult;

        try {
            final byte[] bytes = msg.pop().getData();
            invocationResult = payloadReader.read(InvocationResult.class, bytes);
        } catch (IOException ex) {
            traceLogger.trace("Failed to Parse Response: {}", ex.getMessage());
            throw new InternalException(ex);
        }

        final int part = responseHeader.part.get();
        logger.debug("{} Processing InvocationResult {} for part {}", this, invocationResult, part);

        if (part == 0) {
            syncCompleted = true;
            syncResultConsumer.accept(invocationResult.getResult());
            traceLogger.trace("Got Synchronous Result {}: {}", part, invocationResult.getResult());
            traceLogger.trace("Synchronous Invocation Finished. Result: {}", invocationResult.getResult());
        } else {

            traceLogger.trace("Got Asynchronous Result. Part {}: Result: {}", part, invocationResult.getResult());

            if (!asyncCompleted && (--remaining) == 0) {
                asyncCompleted = true;
                traceLogger.trace("Async invocation finished. Remaining: {}.", remaining);
            } else {
                traceLogger.trace("Async invocation not finished. Remaining: {}", remaining);
            }

            asyncInvocationResultConsumerList.get(part - 1).accept(invocationResult);

        }

    }

    private void handleError(final ZMsg msg,
                             final ResponseHeader responseHeader) {

        logger.debug("Got invocation error.");

        final int part = responseHeader.part.get();
        final InvocationError invocationError = extractInvocationError(msg);

        logger.debug("Processing InvocationError {} for part {}", invocationError, part);

        if (part == 0) {

            final Throwable throwable = invocationError.getThrowable();

            final Exception exception = throwable instanceof Exception ?
                (Exception) throwable :
                new RemoteInvocationException(throwable);

            syncCompleted = true;
            syncErrorConsumer.accept(exception);

            traceLogger.trace("Got Synchronous Error: {}", throwable.getMessage());

        } else if (part == 1) {
            asyncCompleted = true;
            asyncInvocationErrorConsumer.accept(invocationError);
            traceLogger.trace("Got Asynchronous Error: {}. Part: {}", invocationError.getThrowable().getMessage(), part);
        } else {
            asyncCompleted = true;
            traceLogger.trace("Got Invalid Part: {}", part);
            traceLogger.trace("Got Asynchronous Error: {}. Part: {}", invocationError.getThrowable().getMessage(), part);
            throw new InternalException("Invalid error part " + responseHeader.part.get());
        }

    }

    private InvocationError extractInvocationError(final ZMsg msg) {
        try {
            final byte[] bytes = msg.pop().getData();
            return payloadReader.read(InvocationError.class, bytes);
        } catch (Exception e) {
            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(e);
            return invocationError;
        }
    }

    private void handleClose(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

        enterMdc();

        final var cs = asyncOperation.finishCancellation();

        if (CANCELED.equals(cs.getState())) {
            traceLogger.trace("Call canceled. Sending error.");
            syncErrorConsumer.accept(cs.getError());
            asyncInvocationErrorConsumer.acceptAndLogError(logger, cs.getInvocationError());
        } else {
            traceLogger.trace("Call canceled. Refusing to send error.");
        }

        untrace.accept(this);

    }

    private void handleSocketError(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

        enterMdc();

        final int errno = connection.socket().errno();
        final var cs = asyncOperation.requestFinish();
        logger.debug("Got socket error: {}. Sending Error.", errno);

        if (CANCELED.equals(cs.getState()) || CANCELLATION_PENDING.equals(cs.getState())) {
            // We know the operation was canceled. Send the error.
            syncErrorConsumer.accept(cs.getError());
            asyncInvocationErrorConsumer.acceptAndLogError(logger, cs.getInvocationError());
        } else {
            final var ex = new InternalException("Socket error - errno " + errno);
            final var invocationError = new InvocationError();
            invocationError.setThrowable(ex);
            syncErrorConsumer.accept(ex);
            asyncInvocationErrorConsumer.acceptAndLogError(logger, invocationError);
        }

        asyncOperation.finish();
        subscriptions.unsubscribe();
        untrace.accept(this);

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JeroMQRemoteInvocation{");
        sb.append("callId=").append(callId);
        sb.append(", asyncOperation=").append(asyncOperation);
        sb.append(", payloadReader=").append(payloadReader);
        sb.append(", payloadWriter=").append(payloadWriter);
        sb.append(", mdcContext=").append(mdcContext);
        sb.append(", syncResultConsumer=").append(syncResultConsumer);
        sb.append(", syncErrorConsumer=").append(syncErrorConsumer);
        sb.append(", asyncInvocationResultConsumerList=").append(asyncInvocationResultConsumerList);
        sb.append(", asyncInvocationErrorConsumer=").append(asyncInvocationErrorConsumer);
        sb.append(", invocation=").append(invocation);
        sb.append(", additionalCount=").append(additionalCount);
        sb.append(", remaining=").append(remaining);
        sb.append(", syncCompleted=").append(syncCompleted);
        sb.append(", asyncCompleted=").append(asyncCompleted);
        sb.append(", expectedResponseCount=").append(expectedResponseCount);
        sb.append(", subscriptions=").append(subscriptions);
        sb.append('}');
        return sb.toString();
    }

}
