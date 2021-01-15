package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.AsyncConnectionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.Thread.currentThread;

public class JeroMQAsyncConnectionGroup implements AsyncConnectionGroup<ZContext, ZMQ.Socket> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncConnectionGroup.class);

    private final Thread thread = currentThread();

    private final List<AsyncConnection<ZContext, ZMQ.Socket>> connectionList;

    private final BiConsumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>, Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>>> signalHandler;

    public JeroMQAsyncConnectionGroup(
            final List<AsyncConnection<ZContext, ZMQ.Socket>> connectionList,
            final BiConsumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>, Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>>> signalHandler) {
        this.connectionList = connectionList;
        this.signalHandler = signalHandler;
        connectionList.forEach(c -> c.onClose(c0 -> connectionList.remove(c)));
    }

    @Override
    public int size() {
        return connectionList.size();
    }

    @Override
    public AsyncConnection<ZContext, ZMQ.Socket> get(final int index) {
        return connectionList.get(index);
    }

    @Override
    public void close() {

        final CountDownLatch latch = new CountDownLatch(1);

        signal(g -> {

            final List<AsyncConnection<ZContext, ZMQ.Socket>> copy = new ArrayList<>(connectionList);

            copy.forEach(c -> {
                try {
                    c.close();
                } catch (Exception ex) {
                    logger.error("Caught exception closing {}", c);
                }
            });

            latch.countDown();

        });

        try {
            latch.await();
        } catch (InterruptedException ex) {
            logger.error("Interrupted shutting down group {}.", this, ex);
        }

    }

    @Override
    public String toString() {
        return "JeroMQAsyncConnectionGroup{" + thread.getName() + '}';
    }

    @Override
    public void signal(final Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>> consumer) {
        signalHandler.accept(this, consumer);
    }

}
