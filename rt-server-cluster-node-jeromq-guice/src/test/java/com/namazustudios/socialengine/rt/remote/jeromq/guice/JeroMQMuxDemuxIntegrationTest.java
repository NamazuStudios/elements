package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.namazustudios.socialengine.remote.jeromq.JeroMQDemultiplexedConnectionService;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.rt.jeromq.Connection;
import com.namazustudios.socialengine.rt.jeromq.RouteRepresentationUtil;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
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
import java.util.concurrent.ExecutionException;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.zeromq.ZMQ.DEALER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMsg.recvMsg;

public class JeroMQMuxDemuxIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMuxDemuxIntegrationTest.class);

    public static final String CONNECTION_ADDRESS = "inproc://test-connection";

    public static List<UUID> INPROC_IDENTIFIERS = unmodifiableList(range(0, 15)
        .mapToObj(value -> RouteRepresentationUtil.buildInprocIdentifierFromString(format("test-inprocIdentifier-%d", value)))
        .collect(toList()));

    private Thread echoer;

    private ZContext master;

    private ConnectionService multiplexedConnectionService;
    private ConnectionService demultiplexedConnectionService;

    @BeforeClass
    public void setup() {

        master = new ZContext();

        final Injector muxInjector = createInjector(new MuxerModule());
        final Injector demuxInjector = createInjector(new DemuxerModule());

        multiplexedConnectionService = muxInjector.getInstance(ConnectionService.class);
        demultiplexedConnectionService = demuxInjector.getInstance(ConnectionService.class);

        echoer = new Thread(() -> {

            final List<ZMQ.Socket> socketList = INPROC_IDENTIFIERS.stream()
                .map(RouteRepresentationUtil::buildDemultiplexInprocAddress)
                .map(addr -> {
                    final ZMQ.Socket socket = master.createSocket(ZMQ.ROUTER);
                    socket.setRouterMandatory(true);
                    socket.bind(addr);
                    return socket;
                }).collect(toList());

            try (final ZMQ.Poller poller = master.createPoller(socketList.size())){

                socketList.forEach(socket -> poller.register(socket, POLLIN | POLLERR));

                while (!interrupted()) {

                    if (poller.poll(5000) < 0) {
                        break;
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
                        master.destroySocket(socket);
                    } catch (Exception ex) {
                        logger.error("Caught excpetiong shutting down.", ex);
                    }
                });
            }

        });

        echoer.start();

        multiplexedConnectionService.start();
        demultiplexedConnectionService.start();

        INPROC_IDENTIFIERS.forEach(inprocIdentifier -> multiplexedConnectionService.issueBindInprocCommand(null, inprocIdentifier));
        INPROC_IDENTIFIERS.forEach(inprocIdentifier -> demultiplexedConnectionService.issueConnectInprocCommand(null, inprocIdentifier));

    }

    @AfterClass
    public void teardown() throws Exception {
        multiplexedConnectionService.stop();
        multiplexedConnectionService.stop();
        echoer.interrupt();
        echoer.join();
    }

    @DataProvider(parallel = true)
    public static Object[][] destinationIdDataSupplier() {

        return INPROC_IDENTIFIERS
            .stream()
            .map(uuid -> new Object[]{RouteRepresentationUtil.buildMultiplexInprocAddress(uuid)})
            .toArray(Object[][]::new);

    }

    @Test(enabled = false, dataProvider = "destinationIdDataSupplier", invocationCount = 2)
    public void testMuxDemux(final String multiplexedAddress) throws InterruptedException, ExecutionException {

        final UUID uuid = randomUUID();

        try (final ZMQ.Poller poller = master.createPoller(1);
             final Connection connection = from(master, c -> c.createSocket(DEALER))) {

            final int index = poller.register(connection.socket(), POLLIN | POLLERR);
            final boolean connected = connection.socket().connect(multiplexedAddress);
            assertTrue(connected, "Failed to issueOpenBackendChannelCommand.");

            final ZMsg request = new ZMsg();
            request.push(uuid.toString());
            request.push(EMPTY_DELIMITER);
            request.send(connection.socket());

            while (!interrupted()) {

                if (poller.poll(5000) < 0) {
                    break;
                }

                if (poller.pollin(index)) {
                    break;
                } else if (poller.pollerr(index)) {
                    fail("Unxpected socket error." + connection.socket().errno());
                }

            }

            final ZMsg response = recvMsg(connection.socket());

            assertEquals(response.pop().getData().length, 0);
            assertEquals(response.pop().getString(ZMQ.CHARSET), uuid.toString());

        }

    }

    public class MuxerModule extends AbstractModule {

        @Override
        protected void configure() {

            bind(ZContext.class).toInstance(master);

            bind(ConnectionService.class).to(JeroMQMultiplexedConnectionService.class).asEagerSingleton();

        }

    }

    public class DemuxerModule extends AbstractModule {

        @Override
        protected void configure() {

            bind(ZContext.class).toInstance(master);

            bind(String.class)
                .annotatedWith(named(JeroMQDemultiplexedConnectionService.BIND_ADDR))
                .toInstance(CONNECTION_ADDRESS);

            bind(ConnectionService.class).to(JeroMQDemultiplexedConnectionService.class).asEagerSingleton();

        }

    }

}
