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

    private final BiConsumer<SimplePooledAsyncConnection, Consumer<SimplePooledAsyncConnection>> signalHandler;

    private final Publisher<SimplePooledAsyncConnection> onClose = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onRead = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onWrite = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onError = new SimplePublisher<>();

    private final Publisher<SimplePooledAsyncConnection> onRecycle = new SimplePublisher<>();

    public SimplePooledAsyncConnection(
            final ZContext zContext,
            final ZMQ.Socket socket,
            final BiConsumer<SimplePooledAsyncConnection, Consumer<SimplePooledAsyncConnection>> signalHandler) {
        this.zContext = zContext;
        this.socket = socket;
        this.signalHandler = signalHandler;
    }

    @Override
    public <T extends AsyncConnection> Subscription onRead(final Consumer<? super T> asyncConnectionConsumer) {
        return getOnRead().subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends AsyncConnection> Subscription onWrite(Consumer<? super T> asyncConnectionConsumer) {
        return getOnWrite().subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends AsyncConnection> Subscription onError(Consumer<? super T> asyncConnectionConsumer) {
        return getOnError().subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends AsyncConnection> Subscription onClose(Consumer<? super T> asyncConnectionConsumer) {
        return getOnClose().subscribe((Consumer<? super SimplePooledAsyncConnection>) asyncConnectionConsumer);
    }

    @Override
    public <T extends PooledAsyncConnection> Subscription onRecycle(Consumer<? super T> pooledAsyncConnectionConsumer) {
        return getOnRecycle().subscribe((Consumer<? super SimplePooledAsyncConnection>) pooledAsyncConnectionConsumer);
    }

    @Override
    public <T extends AsyncConnection> void signal(Consumer<? super T> asyncConnectionConsumer) {
        signalHandler.accept(this, (Consumer<SimplePooledAsyncConnection>) asyncConnectionConsumer);
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
