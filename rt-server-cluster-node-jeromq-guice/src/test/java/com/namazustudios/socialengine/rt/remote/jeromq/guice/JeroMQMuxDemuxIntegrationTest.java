package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer;
import com.namazustudios.socialengine.rt.ConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.jeromq.Routing;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.List;
import java.util.UUID;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.*;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.zeromq.ZMQ.DEALER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.poll;
import static org.zeromq.ZMsg.recvMsg;

public class JeroMQMuxDemuxIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMuxDemuxIntegrationTest.class);

    public static final String CONNECTION_ADDRESS = "inproc://test-connection";

    public static List<String> DESTINATION_IDS = unmodifiableList(range(0, 15)
        .mapToObj(value -> format("test-destination-%d", value))
        .collect(toList()));

    private Thread echoer;

    private ZContext zContext;

    private ConnectionMultiplexer connectionMultiplexer;

    private ConnectionDemultiplexer connectionDemultiplexer;

    @BeforeClass
    public void setup() {

        zContext = new ZContext();

        final Routing routing = new Routing();
        final Injector muxInjector = createInjector(new MuxerModule(zContext));
        final Injector demuxInjector = createInjector(new DemuxerModule(zContext));

        connectionMultiplexer = muxInjector.getInstance(ConnectionMultiplexer.class);
        connectionDemultiplexer = demuxInjector.getInstance(ConnectionDemultiplexer.class);

        echoer = new Thread(() -> {

            final List<ZMQ.Socket> socketList = DESTINATION_IDS.stream()
                .map(routing::getDestinationId)
                .map(routing::getDemultiplexedAddressForDestinationId)
                .map(addr -> {
                    final ZMQ.Socket socket = zContext.createSocket(ZMQ.ROUTER);
                    socket.setRouterMandatory(true);
                    socket.bind(addr);
                    return socket;
                }).collect(toList());

            try (final ZMQ.Poller poller = zContext.createPoller(socketList.size())){

                socketList.forEach(socket -> poller.register(socket, POLLIN | POLLERR));

                while (!interrupted()) {

                    if (poller.poll(1000) == 0) {
                        continue;
                    }

                    range(0, poller.getNext()).filter(index -> poller.getItem(index) != null).forEach(index -> {

                        final ZMQ.Socket socket = poller.getSocket(index);

                        if (poller.pollin(index)) {
                            final ZMsg msg = recvMsg(socket);
                            msg.send(socket);
                        } else if (poller.pollerr(index)) {
                            logger.error("Error on socket {}", socket.errno());
                        }

                    });

                }

            } finally {
                socketList.forEach(socket -> {
                    try {
                        socket.close();
                        zContext.destroySocket(socket);
                    } catch (Exception ex) {
                        logger.error("Caught excpetiong shutting down.", ex);
                    }
                });
            }

        });

        echoer.start();
        connectionMultiplexer.start();
        connectionDemultiplexer.start();

    }

    @AfterClass
    public void teardown() throws Exception {
        connectionDemultiplexer.stop();
        connectionMultiplexer.stop();
        echoer.interrupt();
        echoer.join();
    }

    @DataProvider(parallel = true)
    public Object[][] destinationIdDataSupplier() {

        final Routing routing = new Routing();

        return DESTINATION_IDS
            .stream()
            .map(id -> routing.getDestinationId(id))
            .map(uuid -> new Object[]{routing.getMultiplexedAddressForDestinationId(uuid)})
            .toArray(Object[][]::new);

    }

    @Test(dataProvider = "destinationIdDataSupplier", threadPoolSize = 10)
    public void testMuxDemux(final String multiplexedAddress) {

        final UUID uuid = randomUUID();

        FinallyAction action = () -> {};

        try (final ZMQ.Socket socket = zContext.createSocket(DEALER);
             final ZMQ.Poller poller = zContext.createPoller(1)) {

            final int index = poller.register(socket, POLLIN | POLLERR);
            final boolean connected = socket.connect(multiplexedAddress);
            assertTrue(connected, "Failed to connect.");

            final ZMsg request = new ZMsg();
            request.push(uuid.toString());
            request.push(EMPTY_DELIMITER);
            request.send(socket);

            while (!interrupted()) {

                if (poller.poll(1000) == 0) {
                    continue;
                } else if (poller.pollin(index)) {
                    break;
                } else if (poller.pollerr(index)) {
                    fail("Unxpected socket error." + socket.errno());
                }

            }

            final ZMsg response = recvMsg(socket);

            assertEquals(response.pop().getData().length, 0);
            assertEquals(response.pop().getString(ZMQ.CHARSET), uuid.toString());

        } finally {
            action.perform();
        }

    }

    public static class MuxerModule extends AbstractModule {

        private final ZContext zContext;

        public MuxerModule(ZContext zContext) {
            this.zContext = zContext;
        }

        @Override
        protected void configure() {

            bind(ZContext.class).toInstance(zContext);

            bind(String.class)
                .annotatedWith(named(JeroMQConnectionMultiplexer.CONNECT_ADDR))
                .toInstance(CONNECTION_ADDRESS);

            final Multibinder<String> destinationIdStringMultibinder;
            destinationIdStringMultibinder = newSetBinder(binder(), String.class, named(JeroMQConnectionMultiplexer.DESTINATION_IDS));
            DESTINATION_IDS.forEach(id -> destinationIdStringMultibinder.addBinding().toInstance(id));

            bind(ConnectionMultiplexer.class).to(JeroMQConnectionMultiplexer.class).asEagerSingleton();

        }

    }

    public static class DemuxerModule extends AbstractModule {

        private final ZContext zContext;

        public DemuxerModule(ZContext zContext) {
            this.zContext = zContext;
        }

        @Override
        protected void configure() {

            bind(ZContext.class).toInstance(zContext);

            bind(String.class)
                .annotatedWith(named(JeroMQConnectionDemultiplexer.BIND_ADDR))
                .toInstance(CONNECTION_ADDRESS);

            final Multibinder<String> destinationIdStringMultibinder;
            destinationIdStringMultibinder = newSetBinder(binder(), String.class, named(JeroMQConnectionDemultiplexer.DESTINATION_IDS));
            DESTINATION_IDS.forEach(id -> destinationIdStringMultibinder.addBinding().toInstance(id));

            bind(ConnectionDemultiplexer.class).to(JeroMQConnectionDemultiplexer.class).asEagerSingleton();

        }

    }

}
