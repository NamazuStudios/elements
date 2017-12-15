package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZPoller;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.Thread.interrupted;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private MessageWriter messageWriter;

    private MessageReader messageReader;

    private ConnectionPool connectionPool;

    @Override
    public void start() {
        getConnectionPool().stop();
    }

    @Override
    public void stop() {
        getConnectionPool().stop();
    }

    @Override
    public Future<Object> invoke(final Invocation invocation,
                                 final Consumer<InvocationError> invocationErrorConsumer,
                                 final List<Consumer<InvocationResult>> invocationResultConsumerList) {

        final CountDownLatch latch = new CountDownLatch(1);

        final AtomicReference<Callable<Object>> resultObjectCallable = new AtomicReference<>(() -> {
            throw new InternalException("Interrupted or request timed out.");
        });

        getConnectionPool().process(connection -> {


            try (final ZPoller poller = new ZPoller(connection.context())) {

                send(connection.socket(), invocation);
                poller.register(connection.socket(), ZPoller.READABLE);

                final int expectedResponseCount = 1 + invocationResultConsumerList.size();

                for (int received = 0; received < expectedResponseCount && !interrupted(); ++received) {


                    poller.poll(-1);

                    final ResponseHeader responseHeader = receiveHeader(connection.socket());

                    handleResponse(connection.socket(),
                                   responseHeader,
                                   invocationErrorConsumer,
                                   result -> resultObjectCallable.set(() -> result.getResult()),
                                   invocationResultConsumerList);

                }

            } catch (Throwable th) {

                resultObjectCallable.set(() -> {
                    logger.error("Could not dispatch invocation.", th);
                    final InvocationError invocationError = new InvocationError();
                    invocationError.setThrowable(th);
                    invocationErrorConsumer.accept(invocationError);
                    throw th;
                });

                latch.countDown();

            }

        });

        return new FutureTask<>(() -> {
            latch.await();
            return resultObjectCallable.get().call();
        });

    }

    private void send(final ZMQ.Socket socket, final Invocation invocation) {
        final byte[] payload = getMessageWriter().write(invocation);
        socket.send(payload);
    }

    private void handleResponse(final ZMQ.Socket socket,
                                final ResponseHeader responseHeader,
                                final Consumer<InvocationError> invocationErrorConsumer,
                                final Consumer<InvocationResult> resultObjectCallable,
                                final List<Consumer<InvocationResult>> invocationResultConsumerList) {

        final int part = responseHeader.part.get();

        switch (responseHeader.type.get()) {
            case INVOCATION_ERROR:
                handleError(socket, invocationErrorConsumer);
                break;
            case INVOCATION_RESULT:
                handleResult(socket, part == 0 ? resultObjectCallable : invocationResultConsumerList.get(part - 1));
                break;
            default:
                logger.error("Invalid response type {}", responseHeader.type.get());
                throw new InternalError("Invalid response type " + responseHeader.type.get());
        }

    }

    private void handleError(final ZMQ.Socket socket, final Consumer<InvocationError> invocationErrorConsumer) {
        final byte[] bytes = socket.recv();
        final InvocationError invocationError = getMessageReader().read(bytes, InvocationError.class);
        invocationErrorConsumer.accept(invocationError);
    }

    private void handleResult(final ZMQ.Socket socket, final Consumer<InvocationResult> invocationResultConsumer) {
        final byte[] bytes = socket.recv();
        final InvocationResult invocationResult = getMessageReader().read(bytes, InvocationResult.class);
        invocationResultConsumer.accept(invocationResult);
    }

    private ResponseHeader receiveHeader(final ZMQ.Socket socket) {

        final byte[] bytes = socket.recv();
        final ResponseHeader responseHeader = new ResponseHeader();

        responseHeader.getByteBuffer().flip();
        responseHeader.getByteBuffer().put(bytes);

        return responseHeader;
    }

    public MessageWriter getMessageWriter() {
        return messageWriter;
    }

    @Inject
    public void setMessageWriter(MessageWriter messageWriter) {
        this.messageWriter = messageWriter;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    @Inject
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public MessageReader getMessageReader() {
        return messageReader;
    }

    @Inject
    public void setMessageReader(MessageReader messageReader) {
        this.messageReader = messageReader;
    }

}
