package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimplePooledAsyncConnection implements PooledAsyncConnection {

    private final ZContext zContext;

    private final ZMQ.Socket socket;

    private final Publisher<SimplePooledAsyncConnection> onClose = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onRead = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onWrite = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onError = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onRecycle = new SimplePublisher<>();

    public SimplePooledAsyncConnection(
            final ZContext zContext,
            final ZMQ.Socket socket) {
        this.zContext = zContext;
        this.socket = socket;
    }

    @Override
    public <T extends AsyncConnection> Subscription onRead(final Consumer<? super T> asyncConnectionConsumer) {
        return onRead.subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends AsyncConnection> Subscription onWrite(Consumer<? super T> asyncConnectionConsumer) {
        return onWrite.subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends AsyncConnection> Subscription onError(Consumer<? super T> asyncConnectionConsumer) {
        return onError.subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends AsyncConnection> Subscription onClose(Consumer<? super T> asyncConnectionConsumer) {
        return onClose.subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends PooledAsyncConnection> Subscription onRecycle(Consumer<? super T> pooledAsyncConnectionConsumer) {
        return onRecycle.subscribe((Consumer<? super SimplePooledAsyncConnection>) pooledAsyncConnectionConsumer);
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

    public void publishOnRecycle() {
        onRecycle.publish(this);
    }

    public Publisher<SimplePooledAsyncConnection> getOnClose() {
        return onClose;
    }

    public Publisher<SimplePooledAsyncConnection> getOnRead() {
        return onRead;
    }

    public Publisher<SimplePooledAsyncConnection> getOnWrite() {
        return onWrite;
    }

    public Publisher<SimplePooledAsyncConnection> getOnError() {
        return onError;
    }

    public Publisher<SimplePooledAsyncConnection> getOnRecycle() {
        return onRecycle;
    }

}
