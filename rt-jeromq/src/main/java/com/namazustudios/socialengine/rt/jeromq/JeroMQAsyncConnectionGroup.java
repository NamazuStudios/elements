package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.AsyncConnectionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JeroMQAsyncConnectionGroup implements AsyncConnectionGroup<ZContext, ZMQ.Socket> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncConnectionGroup.class);

    private final List<AsyncConnection> connectionList;

    private final BiConsumer<AsyncConnectionGroup, Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>>> signalHandler;

    public JeroMQAsyncConnectionGroup(
            final List<AsyncConnection> connectionList,
            final BiConsumer<AsyncConnectionGroup, Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>>> signalHandler) {
        this.connectionList = connectionList;
        this.signalHandler = signalHandler;
        connectionList.forEach(c -> c.onClose(c0 -> connectionList.remove(c)));
    }

    @Override
    public int size() {
        return connectionList.size();
    }

    @Override
    public AsyncConnection get(final int index) {
        return connectionList.get(index);
    }

    @Override
    public void close() {

        final CountDownLatch latch = new CountDownLatch(1);

        signal(g -> {

            connectionList.forEach(c -> {
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
    public void signal(final Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>> consumer) {
        signalHandler.accept(this, consumer);
    }

}
