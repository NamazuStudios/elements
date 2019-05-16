package com.namazustudios.socialengine.rt.jeromq;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.inject.name.Names.named;
import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ConnectionPoolStressTest {

    private static final String ADDRESS = "inproc://mytest";

    private static final int SEND_BATCH = 2000;
    private static final int COOLDOWN_TIME = 20;
    private static final int PARALLEL_TESTS = 500;
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;
    private static final int MINIMUM_CONNECTION_COUNT = 10;
    private static final int MAXIMUM_CONNECTION_COUNT = 5000;

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolStressTest.class);

    public static void main(final String[] args) throws Exception {

        final String type = args.length == 1 ? args[0] : "simple";

        final Injector injector;

        switch (type) {
            case "simple":
                injector = Guice.createInjector(new SimpleConnectionPoolModule());
                break;
            case "dynamic":
                injector = Guice.createInjector(new DynamicConnectionPoolModule());
                break;
            default:
                throw new IllegalArgumentException("Must specify simple or dynamic.");
        }


        final MockServer mockServer = injector.getInstance(MockServer.class);
        mockServer.start();

        final ConnectionPool connectionPool = injector.getInstance(ConnectionPool.class);
        connectionPool.start(zc -> {
            final ZMQ.Socket socket = zc.createSocket(ZMQ.DEALER);
            socket.connect(ADDRESS);
            return socket;
        });

        final ExecutorService executorService = Executors.newCachedThreadPool();

        for (int task = 0; task < PARALLEL_TESTS; ++task) {
            final int t = task;
            executorService.execute(() -> perform(t, connectionPool));
        }

    }

    private static void perform(final int task, final ConnectionPool connectionPool) {
        try {
            while (!interrupted()) {

                // Sends a bunch of messages out the connection which should definitely exceed the number of minimum
                // connections through the pool.
                logger.info("Task {}: Sending batch.", task);
                sendBatch(connectionPool);

                // Waits for the connection pool to drain and close connections that aren't being used.  This should
                // we should see closed connections in the pool.
                logger.info("Task {}: Cooling down.", task);
                sleep(SECONDS.toMillis(COOLDOWN_TIME));

            }
        } catch (InterruptedException ex) {
            logger.info("Interrupted.  Exiting.", ex);
        }
    }

    private static void sendBatch(final ConnectionPool connectionPool) {

        for (int i = 0; i < SEND_BATCH; ++i) {
            connectionPool.process(connection -> {

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

            bind(ZContext.class).toProvider(() -> {
                final ZContext zContext = new ZContext();
                zContext.getContext().setMaxSockets(10000);
                return zContext;
            }).asEagerSingleton();

            bind(ConnectionPool.class).to(DynamicConnectionPool.class).asEagerSingleton();
            bind(Integer.class).annotatedWith(named(ConnectionPool.TIMEOUT)).toInstance(CONNECTION_TIMEOUT_SECONDS);
            bind(Integer.class).annotatedWith(named(ConnectionPool.MIN_CONNECTIONS)).toInstance(MINIMUM_CONNECTION_COUNT);
            bind(Integer.class).annotatedWith(named(ConnectionPool.MAX_CONNECTIONS)).toInstance(MAXIMUM_CONNECTION_COUNT);
        }

    }

    private static class SimpleConnectionPoolModule extends AbstractModule {

        @Override
        protected void configure() {

            bind(ZContext.class).toProvider(() -> {
                final ZContext zContext = new ZContext();
                zContext.getContext().setMaxSockets(10000);
                return zContext;
            }).asEagerSingleton();

            bind(ConnectionPool.class).to(SimpleConnectionPool.class).asEagerSingleton();
            bind(Integer.class).annotatedWith(named(ConnectionPool.TIMEOUT)).toInstance(CONNECTION_TIMEOUT_SECONDS);
            bind(Integer.class).annotatedWith(named(ConnectionPool.MIN_CONNECTIONS)).toInstance(MINIMUM_CONNECTION_COUNT);
            bind(Integer.class).annotatedWith(named(ConnectionPool.MAX_CONNECTIONS)).toInstance(MAXIMUM_CONNECTION_COUNT);
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
            try (final ZContext context = ZContext.shadow(zContext);
                 final ZMQ.Socket socket = context.createSocket(ZMQ.ROUTER);
                 final ZMQ.Poller poller = context.createPoller(1)) {

                socket.bind(ADDRESS);
                final int index = poller.register(socket, ZMQ.Poller.POLLIN);

                while (!interrupted()) {

                    if (poller.poll(5000) < 0) {
                        break;
                    }

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
