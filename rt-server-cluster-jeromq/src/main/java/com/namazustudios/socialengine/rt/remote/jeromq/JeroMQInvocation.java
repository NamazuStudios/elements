package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.*;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import zmq.ZError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.stripCode;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static org.zeromq.ZMQ.SNDMORE;

public class JeroMQInvocation {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInvocation.class);

    private final PayloadReader payloadReader;

    private final PayloadWriter payloadWriter;

    private final ZMQ.Socket socket;

    private final Consumer<HandleResult> handleResultConsumer;

    private final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList;

    private final InvocationErrorConsumer asyncInvocationErrorConsumer;

    private boolean syncCompleted;

    private boolean asyncCompleted;

    private final int expectedResponseCount;

    public JeroMQInvocation(final PayloadReader payloadReader, final PayloadWriter payloadWriter,
                            final ZMQ.Socket socket,
                            final Consumer<HandleResult> handleResultConsumer,
                            final InvocationErrorConsumer asyncInvocationErrorConsumer,
                            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList) {
        this.payloadReader = payloadReader;
        this.payloadWriter = payloadWriter;
        this.socket = socket;
        this.handleResultConsumer = handleResultConsumer;
        this.asyncInvocationResultConsumerList = asyncInvocationResultConsumerList;
        this.asyncInvocationErrorConsumer = asyncInvocationErrorConsumer;
        this.expectedResponseCount = asyncInvocationResultConsumerList.size();
        this.syncCompleted = false;
        this.asyncCompleted = expectedResponseCount == 0;
    }

    public void send(final ZMQ.Socket socket, final Invocation invocation, final int additionalCount) {

        final RequestHeader requestHeader = new RequestHeader();
        requestHeader.additionalParts.set(additionalCount);

        final byte[] payload;

        try {
            payload = payloadWriter.write(invocation);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        socket.send(EMPTY_DELIMITER, SNDMORE);
        socket.sendByteBuffer(requestHeader.getByteBuffer(), SNDMORE);
        socket.send(payload);

    }

    public boolean handle() {

        try {

            final int expectedResponseCount = asyncInvocationResultConsumerList.size();

            HandleResult r = null;
            boolean syncCompleted = false;
            boolean asyncCompleted = expectedResponseCount == 0;

            for (int remaining = expectedResponseCount; !(syncCompleted && asyncCompleted);) {

                final ZMsg msg = recv();

                final HandleResult result = handleResponse(msg);

                switch (result.type) {
                    case SYNC_ERROR:
                    case SYNC_RESULT:
                        handleResultConsumer.accept(r = result);
                        syncCompleted = true;
                        break;
                    case ASYNC_RESULT:
                        if (!asyncCompleted && (--remaining) == 0) {
                            asyncCompleted = true;
                        }
                        break;
                    case ASYNC_ERROR:
                        asyncCompleted = true;
                        break;
                }

            }

            logger.debug("Finished Invocation.");
            if (r == null) throw new InternalException("Got no sync result.");

            return asyncCompleted && syncCompleted;

        } catch (Exception ex) {

            // This is typical of an internal exception (such as a socket error, IO Exception etc.) and should be
            // handed to the clients to ensure that they do not wait around for a response they're never going to
            // get.  So, therefore, the exception is called on both the future task as well as the async handler.
            // For good measure, the exception is re-thrown so the connection pool properly closes the connection
            // as we can't assume that socket is still in a stable state.

            logger.error("Caught error running remote invocation.", ex);

            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(ex);
            asyncInvocationErrorConsumer.acceptAndLogError(logger, invocationError);
            return true;

        }

    }

    private ZMsg recv() {

        final ZMsg zMsg = ZMsg.recvMsg(socket);
        final int error = socket.errno();

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

    private HandleResult handleResponse(final ZMsg msg) {

        final ResponseHeader responseHeader = receiveHeader(msg);

        switch (responseHeader.type.get()) {
            case INVOCATION_RESULT:
                return handleResult(msg, responseHeader);
            case INVOCATION_ERROR:
                return handleError(msg, responseHeader);
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

    private HandleResult handleResult(final ZMsg msg, final ResponseHeader responseHeader) {

        final InvocationResult invocationResult;

        try {
            final byte[] bytes = msg.pop().getData();
            invocationResult = payloadReader.read(InvocationResult.class, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        final int part = responseHeader.part.get();

        if (part == 0) {
            return new HandleResult(HandleResult.Type.SYNC_RESULT, invocationResult.getResult());
        } else {
            asyncInvocationResultConsumerList.get(part - 1).accept(invocationResult);
            return new HandleResult(HandleResult.Type.ASYNC_RESULT);
        }

    }

    private HandleResult handleError(final ZMsg msg,
                                     final ResponseHeader responseHeader) {

        final int part = responseHeader.part.get();
        final InvocationError invocationError = extractInvocationError(msg);

        if (part == 0) {

            final Throwable throwable = invocationError.getThrowable();

            final Exception exception = throwable instanceof Exception ?
                    (Exception) throwable :
                    new RemoteInvocationException(throwable);

            return new HandleResult(HandleResult.Type.SYNC_ERROR, exception);

        } else if (part == 1) {
            asyncInvocationErrorConsumer.accept(invocationError);
            return new HandleResult(HandleResult.Type.ASYNC_ERROR);
        } else {
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

    public static class HandleResult {

        private final HandleResult.Type type;

        private final Object value;

        public HandleResult(final HandleResult.Type type) {

            if (type == null) throw new InternalException("Must specify type.");

            switch (type) {
                case SYNC_ERROR:
                case SYNC_RESULT:
                    throw new InternalException("Must specify ASYNC_ERROR or ASYNC_RESULT");
            }

            this.type = type;
            this.value = null;

        }

        public HandleResult(final HandleResult.Type type, final Object value) {

            if (type == null) throw new InternalException("Must specify type.");

            switch (type) {
                case ASYNC_ERROR:
                case ASYNC_RESULT:
                    throw new InternalException("Must specify SYNC_ERROR or SYNC_RESULT");
                case SYNC_ERROR:
                    if (!(value instanceof Throwable)) throw new InternalException("Must specify Throwable for sync errors.");
            }

            this.type = type;
            this.value = value;

        }

        public Object get() throws Exception {
            switch (type) {
                case SYNC_ERROR:
                    throw ( (value instanceof Exception) ? (Exception) value : new RemoteThrowableException((Throwable)value) );
                case SYNC_RESULT:
                    return value;
                default:
                    throw new InternalException("Unexpected result type.");
            }
        }

        public void get(final CompletableFuture<Object> completableFuture) {
            switch (type) {
                case SYNC_ERROR:
                    completableFuture.completeExceptionally((Throwable)value);
                    break;
                case SYNC_RESULT:
                    completableFuture.complete(value);
                    break;
                default:
                    completableFuture.completeExceptionally(new InternalException("Unexpected result type."));
                    break;
            }
        }

        private enum Type {
            SYNC_RESULT,
            SYNC_ERROR,
            ASYNC_RESULT,
            ASYNC_ERROR,
            TIMEOUT_ERROR,
        }

    }


}
