package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

public class SimpleAsyncConnection implements AsyncConnection {

    private final ZContext zContext;

    private final ZMQ.Socket socket;

    private final Publisher<AsyncConnection> onClose = new SimplePublisher<>();

    private final Publisher<AsyncConnection> onRecycle = new SimplePublisher<>();

    private final Publisher<AsyncConnection> onRead = new SimplePublisher<>();

    private final Publisher<AsyncConnection> onWrite = new SimplePublisher<>();

    private final Publisher<AsyncConnection> onError = new SimplePublisher<>();

    public SimpleAsyncConnection(
            final ZContext zContext,
            final ZMQ.Socket socket) {
        this.zContext = zContext;
        this.socket = socket;
    }

    @Override
    public Subscription onRead(final Consumer<AsyncConnection> asyncConnectionConsumer) {
        return this.onRead.subscribe(asyncConnectionConsumer);
    }

    @Override
    public Subscription onWrite(Consumer<AsyncConnection> asyncConnectionConsumer) {
        return this.onWrite.subscribe(asyncConnectionConsumer);
    }

    @Override
    public Subscription onError(Consumer<AsyncConnection> asyncConnectionConsumer) {
        return this.onError.subscribe(asyncConnectionConsumer);
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
        onClose.publish(this);
    }

    public Publisher<AsyncConnection> getOnClose() {
        return onClose;
    }

    public Publisher<AsyncConnection> getOnRecycle() {
        return onRecycle;
    }

    public Publisher<AsyncConnection> getOnRead() {
        return onRead;
    }

    public Publisher<AsyncConnection> getOnWrite() {
        return onWrite;
    }

    public Publisher<AsyncConnection> getOnError() {
        return onError;
    }

}
