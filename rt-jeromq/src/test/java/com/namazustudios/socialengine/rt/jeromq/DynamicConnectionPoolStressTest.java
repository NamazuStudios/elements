package com.namazustudios.socialengine.rt.jeromq;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DynamicConnectionPoolStressTest {

    private static final String ADDRESS = "inproc://mytest";

    private static final int SEND_BATCH = 1000;
    private static final int COOLDOWN_TIME = 20;
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;
    private static final int MINIMUM_CONNECTION_COUNT = 10;
    private static final int MAXIMUM_CONNECTION_COUNT = 500 ;

    private static final Logger logger = LoggerFactory.getLogger(DynamicConnectionPoolStressTest.class);

    public static void main(final String[] args) throws Exception {

        final Injector injector = Guice.createInjector(new DynamicConnectionPoolModule());

        final MockServer mockServer = injector.getInstance(MockServer.class);
        mockServer.start();

        final ConnectionPool connectionPool = injector.getInstance(ConnectionPool.class);
        connectionPool.start(zc -> {
            final ZMQ.Socket socket = zc.createSocket(ZMQ.DEALER);
            socket.connect(ADDRESS);
            return socket;
        });

        while (!interrupted()) {

            // Sends a bunch of messages out the connection which should definitely exceed the number of minimum
            // connections through the pool.
            logger.info("Sending batch.");
            sendBatch(connectionPool);

            // Waits for the connection pool to drain and close connections that aren't being used.  This should
            // we should see closed connections in the pool.
            logger.info("Cooling down.");
            sleep(SECONDS.toMillis(COOLDOWN_TIME));

        }

    }

    private static void sendBatch(final ConnectionPool connectionPool) {

        for (int i = 0; i < SEND_BATCH; ++i) {
            connectionPool.process(connection -> {

                final String payload = UUID.randomUUID().toString();

                final ZMsg msg = new ZMsg();
                msg.add(UUID.randomUUID().toString());
                msg.send(connection.socket());

                ZMsg.recvMsg(connection.socket()).getFirst().getString(ZMQ.CHARSET);
                return null;

            });
        }

    }

    private static class DynamicConnectionPoolModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ZContext.class).asEagerSingleton();
            bind(ConnectionPool.class).to(DynamicConnectionPool.class).asEagerSingleton();
            bind(Integer.class).annotatedWith(Names.named(DynamicConnectionPool.TIMEOUT)).toInstance(CONNECTION_TIMEOUT_SECONDS);
            bind(Integer.class).annotatedWith(Names.named(DynamicConnectionPool.MIN_CONNECTIONS)).toInstance(MINIMUM_CONNECTION_COUNT);
            bind(Integer.class).annotatedWith(Names.named(DynamicConnectionPool.MAX_CONNECTIONS)).toInstance(MAXIMUM_CONNECTION_COUNT);
        }

    }

    private static class MockServer extends Thread {

        @Inject
        private ZContext zContext;

        public MockServer() {
            setName("MockServer");
        }

        @Override
        public void run() {
            try (final ZMQ.Socket socket = zContext.createSocket(ZMQ.ROUTER);
                 final ZMQ.Poller poller = zContext.createPoller(1)) {

                socket.bind(ADDRESS);
                final int index = poller.register(socket, ZMQ.Poller.POLLIN);

                while (!interrupted()) {
                    poller.poll(1000);
                    doPoll(socket, poller, index);
                }

            } catch (Exception ex) {
                logger.error("Caught error in mock server.", ex);
            }
        }

        private void doPoll(final ZMQ.Socket socket, final ZMQ.Poller poller, final int index) {
            if (poller.pollin(index)) {
                respond(socket);
            }
        }

        private void respond(final ZMQ.Socket socket) {
            final ZMsg zMsg = ZMsg.recvMsg(socket);
            zMsg.send(socket);
        }

    }

}
