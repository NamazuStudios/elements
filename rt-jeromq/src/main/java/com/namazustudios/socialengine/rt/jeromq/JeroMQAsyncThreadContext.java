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
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.String.format;
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

    private final ByteBuffer commandIndexBuffer = ByteBuffer.allocate(Integer.BYTES);

    private final Rollover next = new Rollover(COMMAND_QUEUE_MAX);

    private final AtomicReferenceArray<Runnable> commands = new AtomicReferenceArray<>(COMMAND_QUEUE_MAX);

    private final Semaphore semaphore = new Semaphore(COMMAND_QUEUE_MAX);

    public JeroMQAsyncThreadContext(final ZContext zContext, final ZMQ.Poller poller) {

        try {
            this.pipe = Pipe.open();
            this.pipe.source().configureBlocking(false);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

        final var index = next.getAndIncrement();
        final var output = ByteBuffer.allocate(Integer.BYTES);

        if (!commands.compareAndSet(index, null, command)) {
            logger.error("Buffer overflow at index {}.", index);
            throw new IllegalStateException("Buffer overflow: " + index);
        }

        try {
            output.putInt(index);
            output.flip();
            lock.lock();
            while(output.remaining() > 0) pipe.sink().write(output);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
        if (poller.pollin(commandIndex)) {
            try {
                while (pipe.source().read(commandIndexBuffer) > 0) {
                    if (commandIndexBuffer.remaining() == 0) {

                        commandIndexBuffer.flip();
                        final var idx = commandIndexBuffer.getInt();
                        process(idx);

                        commandIndexBuffer.flip();

                    }
                }
            } catch (IOException ex) {
                throw new InternalException(ex);
            }
        }
    }

    private void pollManagedConnections() {

        final int size = poller.getSize();

        for (int index = connectionIndexStart; index < size; ++index) {
            final var item = (JeroMQThreadContextPollItem) poller.getItem(index);
            if (item != null) item.poll();
        }

    }

    private void process(final int index) {

        final var command = commands.getAndSet(index, null);

        try {
            if (command == null) {
                logger.error("Unable to process command at index {}. Command is null.", index);
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

    public JeroMQAsyncConnection allocateNewConnection(
            final String name,
            final Function<ZContext, ZMQ.Socket> socketSupplier) {

        final var socket = socketSupplier.apply(zContext);

        return new JeroMQAsyncConnection(zContext, poller, socket, this) {

            private final String toString = format("%s {%s}", super.toString(), name);

            @Override
            public String toString() {
                return toString;
            }

        };

    }

    public Subscription onPostLoop(final BiConsumer<Subscription, Void> consumer) {
        return onPostLoop.subscribe(consumer);
    }

}
