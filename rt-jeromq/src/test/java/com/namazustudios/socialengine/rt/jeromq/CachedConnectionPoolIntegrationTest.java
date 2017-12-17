package com.namazustudios.socialengine.rt.jeromq;


import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.inject.name.Names.named;
import static java.lang.Thread.interrupted;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;
import static org.zeromq.ZMsg.*;

@Guice(modules = CachedConnectionPoolIntegrationTest.Module.class)
public class CachedConnectionPoolIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(CachedConnectionPoolIntegrationTest.class);

    private static final String TEST_ADDRESS = "inproc://CachedConnectionPoolIntegrationTest";

    private ZContext zContext;

    private ConnectionPool connectionPool;

    private final CountDownLatch startupLatch = new CountDownLatch(1);

    private final AtomicBoolean acceptingConnections = new AtomicBoolean(true);

    private final Thread acceptor = new Thread(() -> {
        try (final ZMQ.Socket socket = getzContext().createSocket(ZMQ.ROUTER);
             final ZMQ.Poller poller = getzContext().createPoller(1)) {

            socket.bind(TEST_ADDRESS);
            startupLatch.countDown();

            poller.register(socket);

            while (!interrupted() && acceptingConnections.get()) {

                poller.poll(200);

                if (poller.pollin(0)) {
                    recvMsg(socket).send(socket, true);
                } else if (poller.pollerr(0)) {
                    logger.error("Error polling socket {}", socket);
                }

            }

        }
    });

    @BeforeClass
    public void setupListenerSocket() throws InterruptedException {

        logger.info("Starting acceptor thread.");
        acceptor.start();

        logger.info("Waiting startup to complete.");
        startupLatch.await();

        logger.info("Started acceptor.  Starting connection pool.");
        getConnectionPool().start(zContext1 -> {
            final ZMQ.Socket socket = zContext.createSocket(ZMQ.DEALER);
            socket.connect(TEST_ADDRESS);
            return socket;
        });

    }

    @AfterClass()
    public void shutdownListenerSocket() throws InterruptedException {

        logger.info("Stopping connection pool.");
        getConnectionPool().stop();
        logger.info("Connection pool high water mark {}", getConnectionPool().getHighWaterMark());

        logger.info("Stopping acceptor thread.");
        acceptingConnections.set(false);
        acceptor.interrupt();
        acceptor.join();

    }

    @Test(invocationCount = 1000, threadPoolSize = 50)
    public void testParallelInvocations() throws InterruptedException {

        final UUID uuid = randomUUID();
        final ByteBuffer message = ByteBuffer.allocate(16);
        message.putLong(uuid.getLeastSignificantBits());
        message.putLong(uuid.getMostSignificantBits());
        message.flip();

        final BlockingQueue<ByteBuffer> responder = new LinkedBlockingDeque<>();

        getConnectionPool().process(connection -> {

            connection.socket().sendByteBuffer(message, 0);

            final ByteBuffer reply = ByteBuffer.allocate(16);
            connection.socket().recvByteBuffer(reply, 0);

            reply.flip();
            responder.offer(reply);

        });

        final ByteBuffer reply = responder.take();
        assertEquals(message, reply);

    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    @Inject
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {

            binder().bind(ZContext.class).asEagerSingleton();
            binder().bind(ConnectionPool.class).to(CachedConnectionPool.class).asEagerSingleton();

            binder().bind(Integer.class)
                    .annotatedWith(named(CachedConnectionPool.TIMEOUT))
                    .toInstance(60);

            binder().bind(Integer.class)
                    .annotatedWith(named(CachedConnectionPool.MIN_CONNECTIONS))
                    .toInstance(10);

        }
    }

}
