package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.Thread.interrupted;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class JeroMQNode implements Node {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQNode.class);

    private static final String INPROC_BIND_ADDR = "inproc://JeroMQNode/dispatch";

    public static final String BIND_ADDRESS = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.bindAddress";

    public static final String NUMBER_OF_DISPATCHERS = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.numberOfDispatchers";

    private final AtomicReference<Context> context = new AtomicReference<>();

    private ZContext zContext;

    private String  bindAddress;

    private int numberOfDispachers;

    private InvocationDispatcher invocationDispatcher;

    private MessageReader messageReader;

    private MessageWriter messageWriter;

    @Override
    public void start() {

        final Context c = new Context();

        if (context.compareAndSet(null, c)) {
            c.stop();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final Context c = context.get();

        if (context.compareAndSet(c, null)) {
            c.start();
        } else {
            throw new IllegalStateException("Already stopped.");
        }

    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    @Inject
    public void setBindAddress(@Named(BIND_ADDRESS) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getNumberOfDispachers() {
        return numberOfDispachers;
    }

    @Inject
    public void setNumberOfDispachers(@Named(NUMBER_OF_DISPATCHERS) int numberOfDispachers) {
        this.numberOfDispachers = numberOfDispachers;
    }

    public InvocationDispatcher getInvocationDispatcher() {
        return invocationDispatcher;
    }

    @Inject
    public void setInvocationDispatcher(InvocationDispatcher invocationDispatcher) {
        this.invocationDispatcher = invocationDispatcher;
    }

    public MessageReader getMessageReader() {
        return messageReader;
    }

    @Inject
    public void setMessageReader(MessageReader messageReader) {
        this.messageReader = messageReader;
    }

    public MessageWriter getMessageWriter() {
        return messageWriter;
    }

    @Inject
    public void setMessageWriter(MessageWriter messageWriter) {
        this.messageWriter = messageWriter;
    }

    private class Context {

        private final AtomicBoolean running = new AtomicBoolean();

        private final ExecutorService executorService = newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(JeroMQNode.this.getClass().getSimpleName() + " dispatcher thread.");
            return thread;
        });

        public void start() {

            executorService.submit(() -> bindFrontendSocketAndPerformWork());

            final int toDispatch = getNumberOfDispachers();

            for (int i = 0; i < toDispatch; ++i) {
                executorService.submit(() -> connectBackendAndDispatchInvocations());
            }

        }

        public void stop() {

            running.set(false);
            executorService.shutdown();

            try {
                executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down Node.", e);
                throw new InternalException(e);
            }

        }

        private void bindFrontendSocketAndPerformWork() {
            try (final ZMQ.Socket frontend = getzContext().createSocket(ZMQ.ROUTER);
                 final ZMQ.Socket backend = getzContext().createSocket(ZMQ.DEALER);
                 final ZMQ.Poller poller = getzContext().createPoller(2)) {

                frontend.bind(getBindAddress());
                backend.bind(INPROC_BIND_ADDR);

                final int fIndex = poller.register(frontend, ZMQ.Poller.POLLIN);
                final int bIndex = poller.register(backend, ZMQ.Poller.POLLIN);

                while (running.get() && !interrupted()) {

                    final int index = poller.poll();

                    if (index == fIndex) {
                        final ZMsg msg = ZMsg.recvMsg(frontend);
                        msg.send(backend);
                    } else if (index == bIndex) {
                        final ZMsg msg = ZMsg.recvMsg(backend);
                        msg.send(frontend);
                    } else {
                        logger.error("Unexpected poll result {}", index);
                    }

                }

            }
        }

        private void connectBackendAndDispatchInvocations() {
            try (final ZMQ.Socket socket = getzContext().createSocket(ZMQ.ROUTER)) {

                socket.connect(INPROC_BIND_ADDR);

                while (running.get() && !interrupted()) {
                    dispatchMethodInvocation(socket);
                }

            }
        }

        private void dispatchMethodInvocation(final ZMQ.Socket socket) {

            final byte[] identity = socket.recv();
            final byte[] delimiter = socket.recv();

            final RequestHeader requestHeader = new RequestHeader();
            socket.recvByteBuffer(requestHeader.getByteBuffer(), 0);

            final Invocation invocation = getMessageReader().read(socket.recv(), Invocation.class);

            if (delimiter.length > 0) {
                throw new InternalException("Invalid delimiter " + Arrays.toString(delimiter));
            }

            final AtomicInteger remaining = new AtomicInteger(1 + requestHeader.additionalParts.get());

            final Consumer<InvocationError> invocationErrorConsumer = invocationError -> {

                if (remaining.getAndSet(0) > 0) {

                    final ResponseHeader responseHeader = new ResponseHeader();
                    responseHeader.type.set(MessageType.INVOCATION_ERROR);
                    responseHeader.part.set(0);

                    socket.send(identity, ZMQ.SNDMORE);
                    socket.send(delimiter, ZMQ.SNDMORE);
                    socket.sendByteBuffer(responseHeader.getByteBuffer(), ZMQ.SNDMORE);
                    socket.send(getMessageWriter().write(invocationError));

                } else {
                    logger.info("Ignoring other invocation errors.  Terminating context.");
                }

            };

            final Consumer<InvocationResult> returnInvocationResultConsumer = invocationResult -> {
                if (remaining.decrementAndGet() <= 0) {
                    logger.info("Ignoring invocation result {} because of previous errors.", invocationResult);
                } else {

                    final ResponseHeader responseHeader = new ResponseHeader();
                    responseHeader.type.set(MessageType.INVOCATION_RESULT);
                    responseHeader.part.set(0);

                    socket.send(identity, ZMQ.SNDMORE);
                    socket.send(delimiter, ZMQ.SNDMORE);
                    socket.sendByteBuffer(responseHeader.getByteBuffer(), ZMQ.SNDMORE);
                    socket.send(getMessageWriter().write(invocationResult));

                }
            };

            final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList =
                    range(0, requestHeader.additionalParts.get())
                            .map(index -> index + 1)
                            .mapToObj(part -> (Consumer<InvocationResult>) invocationResult -> {

                                if (remaining.decrementAndGet() <= 0) {
                                    logger.info("Ignoring invocation result {} because of previous errors.", invocationResult);
                                } else {

                                    final ResponseHeader responseHeader = new ResponseHeader();
                                    responseHeader.type.set(MessageType.INVOCATION_RESULT);
                                    responseHeader.part.set(part);

                                    socket.send(identity, ZMQ.SNDMORE);
                                    socket.send(delimiter, ZMQ.SNDMORE);
                                    socket.sendByteBuffer(responseHeader.getByteBuffer(), ZMQ.SNDMORE);
                                    socket.send(getMessageWriter().write(invocation));

                                }

                            }).collect(toList());

            getInvocationDispatcher().dispatch(invocation,
                    invocationErrorConsumer,
                    returnInvocationResultConsumer,
                    additionalInvocationResultConsumerList);

        }

    }

}
