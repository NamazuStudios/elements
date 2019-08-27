package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

public class SimplePooledAsyncConnection implements PooledAsyncConnection {

    private final ZContext zContext;

    private final ZMQ.Socket socket;

    private final Publisher<PooledAsyncConnection> onClose = new SimplePublisher<>();

    private final Publisher<PooledAsyncConnection> onRecycle = new SimplePublisher<>();

    private final Publisher<PooledAsyncConnection> onRead = new SimplePublisher<>();

    private final Publisher<PooledAsyncConnection> onWrite = new SimplePublisher<>();

    private final Publisher<PooledAsyncConnection> onError = new SimplePublisher<>();

    public SimplePooledAsyncConnection(
            final ZContext zContext,
            final ZMQ.Socket socket) {
        this.zContext = zContext;
        this.socket = socket;
    }

    @Override
    public Subscription onRead(final Consumer<PooledAsyncConnection> asyncConnectionConsumer) {
        return this.onRead.subscribe(asyncConnectionConsumer);
    }

    @Override
    public Subscription onWrite(Consumer<PooledAsyncConnection> asyncConnectionConsumer) {
        return this.onWrite.subscribe(asyncConnectionConsumer);
    }

    @Override
    public Subscription onError(Consumer<PooledAsyncConnection> asyncConnectionConsumer) {
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

    public Publisher<PooledAsyncConnection> getOnClose() {
        return onClose;
    }

    public Publisher<PooledAsyncConnection> getOnRecycle() {
        return onRecycle;
    }

    public Publisher<PooledAsyncConnection> getOnRead() {
        return onRead;
    }

    public Publisher<PooledAsyncConnection> getOnWrite() {
        return onWrite;
    }

    public Publisher<PooledAsyncConnection> getOnError() {
        return onError;
    }

}
