package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.*;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.remote.*;
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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.stripCode;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static org.zeromq.ZMQ.SNDMORE;

public class JeroMQInvocation {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInvocation.class);

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

    public JeroMQInvocation(final AsyncConnection<ZContext, ZMQ.Socket> connection,
                            final Invocation invocation,
                            final PayloadReader payloadReader, final PayloadWriter payloadWriter,
                            final Map<String, String > mdcContext,
                            final Consumer<Object> syncResultConsumer,
                            final Consumer<Throwable> syncErrorConsumer,
                            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        // Immutable
        this.mdcContext = mdcContext;
        this.payloadReader = payloadReader;
        this.payloadWriter = payloadWriter;
        this.syncResultConsumer = syncResultConsumer;
        this.syncErrorConsumer = syncErrorConsumer;
        this.asyncInvocationResultConsumerList = asyncInvocationResultConsumerList;
        this.asyncInvocationErrorConsumer = asyncInvocationErrorConsumer;
        this.expectedResponseCount = asyncInvocationResultConsumerList.size();
        this.invocation = invocation;
        this.additionalCount = asyncInvocationResultConsumerList.size();

        // Mutable
        this.remaining = expectedResponseCount;
        this.asyncCompleted = expectedResponseCount == 0;

        subscriptions = Subscription.begin()
            .chain(connection.onRead(this::handleRead))
            .chain(connection.onError(this::handleSocketError))
            .chain(connection.onWrite(this::handleWrite));

    }

    private void handleRead(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

        if (mdcContext != null) MDC.setContextMap(mdcContext);

        try {

            final ZMsg msg = recv(connection);
            handleResponse(msg);

            if (asyncCompleted && syncCompleted) {
                logger.debug("Finished Invocation.");
                connection.recycle();
                logger.debug("Recycled Connection.");
                subscriptions.unsubscribe();
                logger.debug("Unsubscribed Events.");
            }

        } catch (Exception ex) {

            // This is typical of an internal exception (such as a socket error, IO Exception etc.) and should be
            // handed to the clients to ensure that they do not wait around for a response they're never going to
            // get.  So, therefore, the exception is called on both the future task as well as the async handler.
            // For good measure, the exception is re-thrown so the connection pool properly closes the connection
            // as we can't assume that socket is still in a stable state.

            logger.error("Caught error running remote invocation.", ex);

            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(ex);

            // We drive the exception to both places appropriately.
            syncErrorConsumer.accept(ex);
            asyncInvocationErrorConsumer.acceptAndLogError(logger, invocationError);

            // Cautiously we should nuke this connection because it coule be placed into an undefined state.
            subscriptions.unsubscribe();
            connection.close();

        }

    }

    private void handleWrite(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

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
            case NO_SUCH_NODE:
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
        } catch (IOException ex) {
            return new InternalException(message, ex);
        } catch (Exception ex) {
            return new InternalException(message, ex);
        }

    }

    private NodeNotFoundException extractNodeNotFoundException(ZMsg zMsg) {

        final Throwable cause = extractException(zMsg);

        try {
            final NodeId nodeId = new NodeId(zMsg.removeLast().getData());
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
                logger.error("Invalid response type {}", responseHeader.type.get());
                throw new InternalException("Invalid response type " + responseHeader.type.get());
        }

    }

    private ResponseHeader receiveHeader(final ZMsg msg) {
        final ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.getByteBuffer().put(msg.pop().getData());
        return responseHeader;
    }

    private void handleResult(final ZMsg msg, final ResponseHeader responseHeader) {

        final InvocationResult invocationResult;

        try {
            final byte[] bytes = msg.pop().getData();
            invocationResult = payloadReader.read(InvocationResult.class, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        final int part = responseHeader.part.get();

        if (part == 0) {
            syncCompleted = true;
            syncResultConsumer.accept(invocationResult.getResult());
        } else {

            if (!asyncCompleted && (--remaining) == 0) {
                asyncCompleted = true;
            }

            asyncInvocationResultConsumerList.get(part - 1).accept(invocationResult);

        }

    }

    private void handleError(final ZMsg msg,
                             final ResponseHeader responseHeader) {

        final int part = responseHeader.part.get();
        final InvocationError invocationError = extractInvocationError(msg);

        if (part == 0) {

            final Throwable throwable = invocationError.getThrowable();

            final Exception exception = throwable instanceof Exception ?
                (Exception) throwable :
                new RemoteInvocationException(throwable);

            syncCompleted = true;
            syncErrorConsumer.accept(exception);

        } else if (part == 1) {
            asyncCompleted = true;
            asyncInvocationErrorConsumer.accept(invocationError);
        } else {
            asyncCompleted = true;
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

    private void handleSocketError(final AsyncConnection<ZContext, ZMQ.Socket> connection) {

        if (mdcContext != null) MDC.setContextMap(mdcContext);

        final int errno = connection.socket().errno();
        final InternalException ex = new InternalException("Socket error - errno " + errno);
        final InvocationError invocationError = new InvocationError();
        invocationError.setThrowable(ex);
        asyncInvocationErrorConsumer.acceptAndLogError(logger, invocationError);
        connection.recycle();
        subscriptions.unsubscribe();

    }

    @Override
    public String toString() {
        return "JeroMQInvocation{" +
                "payloadReader=" + payloadReader +
                ", payloadWriter=" + payloadWriter +
                ", mdcContext=" + mdcContext +
                ", syncResultConsumer=" + syncResultConsumer +
                ", syncErrorConsumer=" + syncErrorConsumer +
                ", asyncInvocationResultConsumerList=" + asyncInvocationResultConsumerList +
                ", asyncInvocationErrorConsumer=" + asyncInvocationErrorConsumer +
                ", invocation=" + invocation +
                ", additionalCount=" + additionalCount +
                ", remaining=" + remaining +
                ", syncCompleted=" + syncCompleted +
                ", asyncCompleted=" + asyncCompleted +
                ", expectedResponseCount=" + expectedResponseCount +
                ", subscriptions=" + subscriptions +
                '}';
    }

}
