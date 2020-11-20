package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.Rollover;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

class JeroMQAsyncThreadContext implements AutoCloseable {

    private static final int COMMAND_QUEUE_MAX = 4096;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncThreadContext.class);
    
    private final int commandIndex;

    private final int connectionIndexStart;

    private final Pipe pipe;

    private final ZMQ.Poller poller;

    private final ZContext zContext;

    private final Publisher<Void> onPostLoop = new SimplePublisher<>();

    private final Lock lock = new ReentrantLock();

    private final ByteBuffer incoming = ByteBuffer.allocate(Integer.BYTES);

    private final Rollover next = new Rollover(COMMAND_QUEUE_MAX);

    private final AtomicReferenceArray<Runnable> commands = new AtomicReferenceArray<>(COMMAND_QUEUE_MAX);

    private final Semaphore semaphore = new Semaphore(COMMAND_QUEUE_MAX);

    public JeroMQAsyncThreadContext(final ZContext zContext, final ZMQ.Poller poller) {

        try {
            this.pipe = Pipe.open();
            this.pipe.source().configureBlocking(false);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        this.poller = poller;
        this.commandIndex = poller.register(pipe.source(), (POLLIN | POLLERR));
        this.connectionIndexStart = poller.getNext();
        this.zContext = zContext;

    }

    public void doInThread(final Runnable command) {

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new InternalException(e);
        }

        final int index = next.getAndIncrement();
        final ByteBuffer output = ByteBuffer.allocate(Integer.BYTES);

        if (!commands.compareAndSet(index, null, command)) {
            logger.info("Buffer overflow at index {}.", index);
            throw new IllegalStateException("Buffer overflow: " + index);
        }

        try {
            output.putInt(index);
            output.flip();
            lock.lock();
            while(output.remaining() > 0) pipe.sink().write(output);
        } catch (IOException ex) {
            throw new InternalException(ex);
        } finally {
            lock.unlock();
        }

    }

    public void poll() {
        pollCommands();
        pollManagedConnections();
        onPostLoop.publish(null);
    }

    private void pollCommands() {

        if (!poller.pollin(commandIndex)) return;
        incoming.position(0).limit(Integer.BYTES);

        try {
            while (pipe.source().read(incoming) > 0) {
                if (incoming.remaining() == 0) {
                    incoming.flip();
                    process(incoming.getInt());
                    incoming.flip();
                }
            }
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }

    private void pollManagedConnections() {
        final int size = poller.getNext();
        for (int index = connectionIndexStart; index < size; ++index) {
            final ThreadContextPollItem item = (ThreadContextPollItem) poller.getItem(index);
            if (item != null) item.poll();
        }
    }

    private void process(final int index) {

        final Runnable command = commands.getAndSet(index, null);

        try {
            if (command == null) {
                logger.error("Unable to process command at index {}", command);
            } else {
                command.run();
            }
        } catch (Exception ex) {
            logger.error("Caught exception processing command {}.", command, ex);
        } finally {
            semaphore.release();
        }

    }

    @Override
    public void close() {

        try {
            pipe.sink().close();
        } catch (IOException ex) {
            logger.error("Error closing sink.", ex);
        }

        try {
            pipe.source().close();
        } catch (IOException ex) {
            logger.error("Error closing source.", ex);
        }

    }

    public JeroMQAsyncConnection allocateNewConnection(final Function<ZContext, ZMQ.Socket> socketSupplier) {

        final ZMQ.Socket socket = socketSupplier.apply(zContext);

        final JeroMQAsyncConnection.FlagChangeHandler flagChangeHandler = (conn, flags) -> onPostLoop.subscribe((subscription, v) ->  {

            poller.unregister(socket);

            if (flags != 0) {
                final ThreadContextPollItem item = new ThreadContextPollItem(conn, flags);
                poller.register(item);
            }

            subscription.unsubscribe();

        });

        final JeroMQAsyncConnection connection = new JeroMQAsyncConnection(
            zContext, socket,
            flagChangeHandler,
            (conn, consumer) -> doInThread(() -> consumer.accept(conn)));

        connection.onClose(c -> onPostLoop.subscribe((subscriber, v) -> {
            poller.unregister(socket);
            socket.close();
            subscriber.unsubscribe();
        }));

        return connection;

    }

    public Subscription onPostLoop(final BiConsumer<Subscription, Void> consumer) {
        return onPostLoop.subscribe(consumer);
    }

    private static class ThreadContextPollItem extends ZMQ.PollItem {

        private final JeroMQAsyncConnection connection;

        public ThreadContextPollItem(final JeroMQAsyncConnection connection, int ops) {
            super(connection.socket(), ops);
            this.connection = connection;
        }

        public void poll() {
            if (isError()) connection.getOnError().publish(connection);
            if (isReadable()) connection.getOnRead().publish(connection);
            if (isWritable()) connection.getOnWrite().publish(connection);
        }

    }

}
