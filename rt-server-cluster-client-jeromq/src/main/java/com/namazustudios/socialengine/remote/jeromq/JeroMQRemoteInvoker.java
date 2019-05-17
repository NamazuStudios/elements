package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.RemoteThrowableException;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZPoller;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static java.lang.Thread.interrupted;
import static org.zeromq.ZMQ.SNDMORE;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    /**
     * The connect address for use with the {@link JeroMQRemoteInvoker}
     */
    public static final String CONNECT_ADDRESS = "com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.connectAddress";

    /**
     * Specifies an {@link ExecutorService} used to run the asynchronous tasks in the {@link RemoteInvoker}
     */
    public static final String ASYNC_EXECUTOR_SERVICE = "com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.executor";

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private String connectAddress;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private ConnectionPool connectionPool;

    private ExecutorService executorService;

    @Override
    public void start() {
        getConnectionPool().start(zContext -> {
            final ZMQ.Socket socket = zContext.createSocket(ZMQ.DEALER);
            socket.connect(getConnectAddress());
            return socket;
        }, JeroMQRemoteInvoker.class.getName());
    }

    @Override
    public void stop() {
        getConnectionPool().stop();
    }

    @Override
    public Future<Object> invokeFuture(final Invocation invocation,
                                       final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                       final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final Map<String, String > mdcContext = MDC.getCopyOfContextMap();
        final CompletableFuture<Object> completableFuture = new CompletableFuture<>();

        getExecutorService().submit(() -> {

            if (mdcContext != null) MDC.setContextMap(mdcContext);

            try {
                doInvoke(invocation,
                         hr -> hr.get(completableFuture),
                         asyncInvocationResultConsumerList,
                         asyncInvocationErrorConsumer);
            } catch (Exception ex) {
                completableFuture.completeExceptionally(ex);
                logger.error("Caught exception processing invocation.", ex);
            }finally {
                MDC.clear();
            }

        });

        return completableFuture;

    }

    @Override
    public Object invokeSync(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {
        return doInvoke(invocation,
                        hr -> logger.debug("Got sync return value: {}", hr),
                        asyncInvocationResultConsumerList,
                        asyncInvocationErrorConsumer);
    }

    public Object doInvoke(
            final Invocation invocation,
            final Consumer<HandleResult> resultConsumer,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        final HandleResult syncResult = getConnectionPool().process(connection -> {

            try (final ZMQ.Poller poller = connection.context().createPoller(1)) {

                send(connection.socket(), invocation, asyncInvocationResultConsumerList.size());
                final int sIndex = poller.register(connection.socket(), ZPoller.READABLE);

                final int expectedResponseCount = asyncInvocationResultConsumerList.size();

                HandleResult r = null;
                boolean syncCompleted = false;
                boolean asyncCompleted = expectedResponseCount == 0;

                for (int remaining = expectedResponseCount; !(syncCompleted && asyncCompleted);) {

                    if (!pollForResponse(poller, sIndex)) {
                        break;
                    }

                    final ZMsg msg = ZMsg.recvMsg(connection.socket());
                    msg.pop();

                    final HandleResult result = handleResponse(
                            msg,
                            asyncInvocationResultConsumerList,
                            asyncInvocationErrorConsumer);

                    switch (result.type) {
                        case SYNC_ERROR:
                        case SYNC_RESULT:
                            resultConsumer.accept(r = result);
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

                return r;

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
                throw ex;

            }

        });

        return syncResult.get();

    }

    private boolean pollForResponse(final ZMQ.Poller poller, final int sIndex) {

        while (!interrupted()) {

            if (poller.poll(5000) < 0) {
                throw new InternalException("Interrupted.  Shutting down.");
            }

            if (poller.pollin(sIndex)) {
                return true;
            } else if (poller.pollerr(sIndex)) {
                final ZMQ.Socket socket = poller.getSocket(sIndex);
                final String error = socket == null ? "UNKNOWN" : Integer.toString(socket.errno());
                logger.error("Socket Error {}" + error);
                throw new InternalException("Socket error: " + error);
            }

        }

        return false;

    }

    private void send(final ZMQ.Socket socket, final Invocation invocation, final int additionalCount) {

        final RequestHeader requestHeader = new RequestHeader();
        requestHeader.additionalParts.set(additionalCount);

        final byte[] payload;

        try {
            payload = getPayloadWriter().write(invocation);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        socket.send(EMPTY_DELIMITER, SNDMORE);
        socket.sendByteBuffer(requestHeader.getByteBuffer(), SNDMORE);
        socket.send(payload);

    }

    private HandleResult handleResponse(final ZMsg msg,
                                        final List<Consumer<InvocationResult>> asyncResultConsumerList,
                                        final InvocationErrorConsumer asyncErrorConsumer) {

        final ResponseHeader responseHeader = receiveHeader(msg);

        switch (responseHeader.type.get()) {
            case INVOCATION_RESULT:
                return handleResult(msg, responseHeader, asyncResultConsumerList);
            case INVOCATION_ERROR:
                return handleError(msg, responseHeader, asyncErrorConsumer);
            default:
                logger.error("Invalid response type {}", responseHeader.type.get());
                throw new InternalException("Invalid response type " + responseHeader.type.get());
        }

    }

    private HandleResult handleError(final ZMsg msg,
                                     final ResponseHeader responseHeader,
                                     final InvocationErrorConsumer asyncErrorConsumer) {

        final int part = responseHeader.part.get();
        final InvocationError invocationError = extractInvocationError(msg);

        if (part == 0) {

            final Throwable throwable = invocationError.getThrowable();

            final Exception exception = throwable instanceof Exception ?
                (Exception) throwable :
                new RemoteInvocationException(throwable);

            return new HandleResult(HandleResult.Type.SYNC_ERROR, exception);

        } else if (part == 1) {
            asyncErrorConsumer.accept(invocationError);
            return new HandleResult(HandleResult.Type.ASYNC_ERROR);
        } else {
            throw new InternalException("Invalid error part " + responseHeader.part.get());
        }

    }

    private InvocationError extractInvocationError(final ZMsg msg) {
        try {
            final byte[] bytes = msg.pop().getData();
            return getPayloadReader().read(InvocationError.class, bytes);
        } catch (Exception e) {
            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(e);
            return invocationError;
        }
    }

    private HandleResult handleResult(final ZMsg msg,
                                      final ResponseHeader responseHeader,
                                      final List<Consumer<InvocationResult>> asyncResultConsumerList) {

        final InvocationResult invocationResult;

        try {
            final byte[] bytes = msg.pop().getData();
            invocationResult = getPayloadReader().read(InvocationResult.class, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        final int part = responseHeader.part.get();

        if (part == 0) {
            return new HandleResult(HandleResult.Type.SYNC_RESULT, invocationResult.getResult());
        } else {
            asyncResultConsumerList.get(part - 1).accept(invocationResult);
            return new HandleResult(HandleResult.Type.ASYNC_RESULT);
        }

    }

    private ResponseHeader receiveHeader(final ZMsg msg) {
        final ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.getByteBuffer().put(msg.pop().getData());
        return responseHeader;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    @Inject
    public void setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
    }

    public PayloadWriter getPayloadWriter() {
        return payloadWriter;
    }

    @Inject
    public void setPayloadWriter(PayloadWriter payloadWriter) {
        this.payloadWriter = payloadWriter;
    }

    public String getConnectAddress() {
        return connectAddress;
    }

    @Inject
    public void setConnectAddress(@Named(CONNECT_ADDRESS) String connectAddress) {
        this.connectAddress = connectAddress;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    @Inject
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Inject
    public void setExecutorService(@Named(ASYNC_EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = executorService;
    }

    private static class HandleResult {

        private final Type type;

        private final Object value;

        public HandleResult(final Type type) {

            if (type == null) throw new InternalException("Must specify type.");

            switch (type) {
                case SYNC_ERROR:
                case SYNC_RESULT:
                    throw new InternalException("Must specify ASYNC_ERROR or ASYNC_RESULT");
            }

            this.type = type;
            this.value = null;

        }

        public HandleResult(final Type type, final Object value) {

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
            ASYNC_ERROR
        }

    }

}
