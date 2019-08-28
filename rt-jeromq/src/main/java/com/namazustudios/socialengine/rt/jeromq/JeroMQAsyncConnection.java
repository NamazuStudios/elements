package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.zeromq.ZMQ.Poller.*;

public class JeroMQAsyncConnection extends ZMQ.PollItem implements AsyncConnection<ZContext, ZMQ.Socket> {

    private final ZContext zContext;

    private final ZMQ.Socket socket;

    private final BiConsumer<JeroMQAsyncConnection, Consumer<AsyncConnection<ZContext, ZMQ.Socket>>> signalHandler;

    private final Publisher<JeroMQAsyncConnection> onClose = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onRead = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onWrite = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onError = new SimplePublisher<>();

    private final Publisher<JeroMQAsyncConnection> onRecycle = new SimplePublisher<>();

    public JeroMQAsyncConnection(
            final ZContext zContext,
            final ZMQ.Socket socket,
            final BiConsumer<JeroMQAsyncConnection, Consumer<AsyncConnection<ZContext, ZMQ.Socket>>> signalHandler) {
        super(socket, POLLIN| POLLOUT | POLLERR);
        this.zContext = zContext;
        this.socket = socket;
        this.signalHandler = signalHandler;
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
