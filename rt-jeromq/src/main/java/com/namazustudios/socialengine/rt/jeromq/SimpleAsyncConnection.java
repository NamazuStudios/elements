package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.zeromq.ZMQ.Poller.*;

public class SimpleAsyncConnection extends ZMQ.PollItem implements AsyncConnection {

    private final ZContext zContext;

    private final ZMQ.Socket socket;

    private final BiConsumer<SimpleAsyncConnection, Consumer<AsyncConnection>> signalHandler;

    private final Publisher<SimpleAsyncConnection> onClose = new SimplePublisher<>();

    private final Publisher<SimpleAsyncConnection> onRead = new SimplePublisher<>();

    private final Publisher<SimpleAsyncConnection> onWrite = new SimplePublisher<>();

    private final Publisher<SimpleAsyncConnection> onError = new SimplePublisher<>();

    private final Publisher<SimpleAsyncConnection> onRecycle = new SimplePublisher<>();

    public SimpleAsyncConnection(
            final ZContext zContext,
            final ZMQ.Socket socket,
            final BiConsumer<SimpleAsyncConnection, Consumer<AsyncConnection>> signalHandler) {
        super(socket, POLLIN| POLLOUT | POLLERR);
        this.zContext = zContext;
        this.socket = socket;
        this.signalHandler = signalHandler;
    }

    @Override
    public  Subscription onRead(final Consumer<AsyncConnection> asyncConnectionConsumer) {
        return onRead.subscribe(asyncConnectionConsumer);
    }

    @Override
    public  Subscription onWrite(Consumer<AsyncConnection> asyncConnectionConsumer) {
        return onWrite.subscribe(asyncConnectionConsumer);
    }

    @Override
    public  Subscription onError(Consumer<AsyncConnection> asyncConnectionConsumer) {
        return onError.subscribe(asyncConnectionConsumer);
    }

    @Override
    public  Subscription onClose(Consumer<AsyncConnection> asyncConnectionConsumer) {
        return onClose.subscribe(asyncConnectionConsumer);
    }

    @Override
    public Subscription onRecycle(Consumer<AsyncConnection> pooledAsyncConnectionConsumer) {
        return onRecycle.subscribe(pooledAsyncConnectionConsumer);
    }

    @Override
    public void signal(final Consumer<AsyncConnection> asyncConnectionConsumerConsumer) {
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

    public Publisher<SimpleAsyncConnection> getOnClose() {
        return onClose;
    }

    public Publisher<SimpleAsyncConnection> getOnRead() {
        return onRead;
    }

    public Publisher<SimpleAsyncConnection> getOnWrite() {
        return onWrite;
    }

    public Publisher<SimpleAsyncConnection> getOnError() {
        return onError;
    }

    public Publisher<SimpleAsyncConnection> getOnRecycle() {
        return onRecycle;
    }

}
