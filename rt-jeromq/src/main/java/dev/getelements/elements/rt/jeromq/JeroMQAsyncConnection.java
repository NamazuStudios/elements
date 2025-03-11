package dev.getelements.elements.rt.jeromq;

import dev.getelements.elements.rt.AsyncConnection;
import dev.getelements.elements.sdk.util.Publisher;
import dev.getelements.elements.sdk.util.LinkedPublisher;
import dev.getelements.elements.sdk.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

import static java.lang.Thread.currentThread;

class JeroMQAsyncConnection implements AsyncConnection<ZContext, ZMQ.Socket> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncConnection.class);

    private final String toString;

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final ZMQ.Socket socket;

    private final JeroMQAsyncThreadContext asyncThreadContext;

    private final Publisher<JeroMQAsyncConnection> onClose = new LinkedPublisher<>();

    private final Publisher<JeroMQAsyncConnection> onRead = new LinkedPublisher<>();

    private final Publisher<JeroMQAsyncConnection> onWrite = new LinkedPublisher<>();

    private final Publisher<JeroMQAsyncConnection> onError = new LinkedPublisher<>();

    private final Publisher<JeroMQAsyncConnection> onRecycle = new LinkedPublisher<>();

    public JeroMQAsyncConnection(
            final ZContext zContext,
            final ZMQ.Poller poller,
            final ZMQ.Socket socket,
            final JeroMQAsyncThreadContext asyncThreadContext) {
        this.zContext = zContext;
        this.poller = poller;
        this.socket = socket;
        this.asyncThreadContext = asyncThreadContext;
        this.toString = "JeroMQAsyncConnection{thread: '" + currentThread().getName() + "'}";
    }

    @Override
    public void setEvents(final Event ... events) {

        var flags = 0;

        for (final var event : events) {
            switch (event) {
                case READ:  flags |= ZMQ.Poller.POLLIN; break;
                case WRITE: flags |= ZMQ.Poller.POLLOUT; break;
                case ERROR: flags |= ZMQ.Poller.POLLERR; break;
            }
        }

        poller.unregister(socket);

        if (flags != 0) {
            final var item = new JeroMQThreadContextPollItem(this, flags);
            poller.register(item);
        }

    }

    @Override
    public  Subscription onRead(final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {
        return onRead.subscribe(asyncConnectionConsumer);
    }

    @Override
    public  Subscription onWrite(Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {
        return onWrite.subscribe(asyncConnectionConsumer);
    }

    @Override
    public  Subscription onError(Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {
        return onError.subscribe(asyncConnectionConsumer);
    }

    @Override
    public  Subscription onClose(Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {
        return onClose.subscribe(asyncConnectionConsumer);
    }

    @Override
    public Subscription onRecycle(Consumer<AsyncConnection<ZContext, ZMQ.Socket>> pooledAsyncConnectionConsumer) {
        return onRecycle.subscribe(pooledAsyncConnectionConsumer);
    }

    @Override
    public void signal(final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumerConsumer) {
        asyncThreadContext.doInThread(() -> asyncConnectionConsumerConsumer.accept(this));
    }

    @Override
    public void recycle() {
        onRecycle.publish(this);
    }

    @Override
    public ZContext context() {
        return zContext;
    }

    @Override
    public ZMQ.Socket socket() {
        return socket;
    }

    @Override
    public void close() {

        onClose.publish(this);

        try {
            poller.unregister(socket);
        } catch (Exception ex) {
            logger.error("Caught exception unregistering socket.", ex);
        }

        try {
            socket.close();
        } catch (Exception ex) {
            logger.error("Caught exception closing socket.", ex);
        }

    }

    @Override
    public String toString() {
        return toString;
    }

    public Publisher<JeroMQAsyncConnection> getOnClose() {
        return onClose;
    }

    public Publisher<JeroMQAsyncConnection> getOnRead() {
        return onRead;
    }

    public Publisher<JeroMQAsyncConnection> getOnWrite() {
        return onWrite;
    }

    public Publisher<JeroMQAsyncConnection> getOnError() {
        return onError;
    }

    public Publisher<JeroMQAsyncConnection> getOnRecycle() {
        return onRecycle;
    }

}
