package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static java.lang.Thread.currentThread;

class JeroMQAsyncConnection implements AsyncConnection<ZContext, ZMQ.Socket> {

    private static final boolean CHECK_THREAD = true;

    private final Thread thread = currentThread();

    private final Runnable threadChecker = CHECK_THREAD ? () -> {
        if (!currentThread().equals(thread)) throw new IllegalStateException("Wrong thread.");
    } : () -> {};

    private final ZContext zContext;

    private final ZMQ.Socket socket;

    private final FlagChangeHandler flagChangeHandler;

    private final BiConsumer<JeroMQAsyncConnection, Consumer<AsyncConnection<ZContext, ZMQ.Socket>>> signalHandler;

    private final Publisher<JeroMQAsyncConnection> onClose = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onRead = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onWrite = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onError = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onRecycle = new SimplePublisher<>();

    public JeroMQAsyncConnection(
            final ZContext zContext,
            final ZMQ.Socket socket,
            final FlagChangeHandler flagChangeHandler,
            final BiConsumer<JeroMQAsyncConnection, Consumer<AsyncConnection<ZContext, ZMQ.Socket>>> signalHandler) {
        this.flagChangeHandler = flagChangeHandler;
        this.zContext = zContext;
        this.socket = socket;
        this.signalHandler = signalHandler;
    }

    @Override
    public void clearEvents() {
        flagChangeHandler.onFlagChange(this, 0);
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

        flagChangeHandler.onFlagChange(this, flags);

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
        signalHandler.accept(this, asyncConnectionConsumerConsumer);
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
        socket().close();
        getOnClose().publish(this);
    }

    @Override
    public String toString() {
        return "JeroMQAsyncConnection{thread: '" + thread.getName() + "'}";
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

    @FunctionalInterface
    interface FlagChangeHandler {

        void onFlagChange(JeroMQAsyncConnection connection, int flags);

    }

}
