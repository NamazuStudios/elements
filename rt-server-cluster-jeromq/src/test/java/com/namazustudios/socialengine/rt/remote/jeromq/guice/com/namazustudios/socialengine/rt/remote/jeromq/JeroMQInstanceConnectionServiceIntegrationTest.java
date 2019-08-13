package com.namazustudios.socialengine.rt.remote.jeromq.guice.com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceConnection;
import com.namazustudios.socialengine.rt.remote.InstanceStatus;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlClient;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.BIND_ADDRESS;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.zeromq.SocketType.DEALER;
import static org.zeromq.ZContext.shadow;

@Guice(modules = JeroMQInstanceConnectionServiceIntegrationTest.Module.class)
public class JeroMQInstanceConnectionServiceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInstanceConnectionServiceIntegrationTest.class);

    private static final String BIND_URL_FIRST = "inproc://bind-first";

    private static final String BIND_URL_SECOND = "inproc://bind-second";

    private ZContext zContext;

    private Supplier<RemoteInvoker> remoteInvokerSupplier;

    private MockInstanceDiscoveryService mockInstanceDiscoveryService;

    private InstanceConnectionService firstInstanceConnectionService;

    private InstanceConnectionService secondInstanceConnectionService;

    private final List<ApplicationId> mockApplicationIds = unmodifiableList(asList(
        randomApplicationId(), randomApplicationId(), randomApplicationId(), randomApplicationId()
    ));

    private final Map<String, List<RemoteInvoker>> mockRemoteInvokers = new ConcurrentHashMap<>();

    private final ExecutorService executorService = newCachedThreadPool(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(JeroMQInstanceConnectionServiceIntegrationTest.class.getSimpleName());
        thread.setUncaughtExceptionHandler((t, e) -> logger.error("Caught Exception {}", t, e));
        return thread;
    });

    @Test
    public void testStart() {

        when(getRemoteInvokerSupplier().get()).thenAnswer(i0 -> {

            final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);

            doAnswer(i1 -> {
                final String connectAddress = i1.getArgument(0);
                mockRemoteInvokers.computeIfAbsent(connectAddress, a -> new CopyOnWriteArrayList<>());
                return null;
            }).when(mockRemoteInvoker).start(anyString());

            return mockRemoteInvoker;

        });

        getFirstInstanceConnectionService().start();
        getSecondInstanceConnectionService().start();

    }

    @Test(expectedExceptions = IllegalStateException.class, dependsOnMethods = "testStart")
    public void testStartTwiceThrowsFirst() {
        getFirstInstanceConnectionService().start();
    }

    @Test(expectedExceptions = IllegalStateException.class, dependsOnMethods = "testStart")
    public void testStartTwiceThrowsSecond() {
        getSecondInstanceConnectionService().start();
    }

    @Test(dependsOnMethods = "testStart")
    public void testOpenBindingsWithEchoServers() {

        for (final ApplicationId mockApplicationId : mockApplicationIds) {
            runEchoServer(getFirstInstanceConnectionService(), mockApplicationId);
            runEchoServer(getSecondInstanceConnectionService(), mockApplicationId);
        }

        assertNodeStatusIsCorrect(getFirstInstanceConnectionService());
        assertNodeStatusIsCorrect(getSecondInstanceConnectionService());

    }

    private void runEchoServer(final InstanceConnectionService instanceConnectionService, final ApplicationId applicationId) {

        final CountDownLatch latch = new CountDownLatch(1);

        final Supplier<InstanceBinding> instanceBindingSupplier = () -> {
            final NodeId nodeId = new NodeId(instanceConnectionService.getInstanceId(), applicationId);
            return instanceConnectionService.openBinding(nodeId);
        };

        executorService.submit(() -> {
            try (final JeroMQEchoServer svr = new JeroMQEchoServer(getzContext(), instanceBindingSupplier)) {
                svr.run(latch::countDown);
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted while waiting on echo service to start", e);
        }

    }

    private void assertNodeStatusIsCorrect(final InstanceConnectionService instanceConnectionService) {

        final String controlAddress = instanceConnectionService.getLocalControlAddress();

        try (ControlClient client = new JeroMQControlClient(getzContext(), controlAddress)) {

            final InstanceStatus instanceStatus = client.getInstanceStatus();
            final Set<NodeId> instanceStatusSet = new HashSet<>(instanceStatus.getNodeIds());

            final Set<NodeId> mockNodeIdSet = mockApplicationIds
                .stream()
                .map(aid -> new NodeId(instanceConnectionService.getInstanceId(), aid))
                .collect(toSet());

            assertEquals(instanceStatusSet, mockNodeIdSet);
            assertEquals(instanceStatus.getInstanceId(), instanceConnectionService.getInstanceId());

        }

    }

    @Test(dependsOnMethods = "testOpenBindingsWithEchoServers")
    public void testAddConnections() throws Exception {

        final CountDownLatch countDownLatch = new CountDownLatch(4 * mockApplicationIds.size());

        getFirstInstanceConnectionService().subscribeToConnect(ic -> mockApplicationIds.forEach(aid -> {
            final NodeId nodeId = new NodeId(ic.getInstanceId(), aid);
            testRoundTripForNode(ic, nodeId);
            countDownLatch.countDown();
        }));

        getSecondInstanceConnectionService().subscribeToConnect(ic -> mockApplicationIds.forEach(aid -> {
            final NodeId nodeId = new NodeId(ic.getInstanceId(), aid);
            testRoundTripForNode(ic, nodeId);
            countDownLatch.countDown();
        }));

        getMockInstanceDiscoveryService().addHosts(BIND_URL_FIRST, BIND_URL_SECOND);
        countDownLatch.await();

    }

    public void testRoundTripForNode(final InstanceConnection instanceConnection, final NodeId nodeId) {
        try (final ZContext context = shadow(getzContext());
             final ZMQ.Socket socket = context.createSocket(DEALER)) {

            final String connectAddress = instanceConnection.openRouteToNode(nodeId);
            socket.connect(connectAddress);

            final UUID uuid = randomUUID();
            final ZMsg zMsg = new ZMsg();
            zMsg.addFirst(uuid.toString().getBytes(CHARSET));
            zMsg.addFirst(EMPTY_DELIMITER);
            zMsg.send(socket);

            final ZMsg response = ZMsg.recvMsg(socket);
            assertNotNull(response);

            final byte[] delimiter = response.removeFirst().getData();
            assertEquals(delimiter, EMPTY_DELIMITER);
            assertEquals(fromString(response.getFirst().getString(CHARSET)), uuid);

        }
    }

    @Test(dependsOnMethods = "testAddConnections")
    public void testRemoveConnections() throws Exception {

        final CountDownLatch countDownLatch = new CountDownLatch(4);
        getFirstInstanceConnectionService().subscribeToDisconnect(ic -> countDownLatch.countDown());
        getFirstInstanceConnectionService().subscribeToDisconnect(ic -> countDownLatch.countDown());

        getMockInstanceDiscoveryService().removeHosts(BIND_URL_FIRST, BIND_URL_SECOND);

        countDownLatch.await();
        assertTrue(getFirstInstanceConnectionService().getActiveConnections().isEmpty());
        assertTrue(getSecondInstanceConnectionService().getActiveConnections().isEmpty());

    }

    @Test(dependsOnMethods = {
            "testStartTwiceThrowsFirst",
            "testStartTwiceThrowsSecond",
            "testOpenBindingsWithEchoServers",
            "testAddConnections",
            "testRemoveConnections"
    })
    public void testStop() {
        getFirstInstanceConnectionService().stop();
        getSecondInstanceConnectionService().stop();
    }

    @Test(dependsOnMethods = "testStop", expectedExceptions = IllegalStateException.class)
    public void testDoubleStopFirst() {
        getFirstInstanceConnectionService().stop();
    }

    @Test(dependsOnMethods = "testStop", expectedExceptions = IllegalStateException.class)
    public void testDoubleStopSecond() {
        getSecondInstanceConnectionService().stop();
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public Supplier<RemoteInvoker> getRemoteInvokerSupplier() {
        return remoteInvokerSupplier;
    }

    @Inject
    public void setRemoteInvokerSupplier(Supplier<RemoteInvoker> remoteInvokerSupplier) {
        this.remoteInvokerSupplier = remoteInvokerSupplier;
    }

    public MockInstanceDiscoveryService getMockInstanceDiscoveryService() {
        return mockInstanceDiscoveryService;
    }

    @Inject
    public void setMockInstanceDiscoveryService(MockInstanceDiscoveryService mockInstanceDiscoveryService) {
        this.mockInstanceDiscoveryService = mockInstanceDiscoveryService;
    }

    public InstanceConnectionService getFirstInstanceConnectionService() {
        return firstInstanceConnectionService;
    }

    @Inject
    public void setFirstInstanceConnectionService(@Named(BIND_URL_FIRST) InstanceConnectionService firstInstanceConnectionService) {
        this.firstInstanceConnectionService = firstInstanceConnectionService;
    }

    public InstanceConnectionService getSecondInstanceConnectionService() {
        return secondInstanceConnectionService;
    }

    @Inject
    public void setSecondInstanceConnectionService(@Named(BIND_URL_SECOND) InstanceConnectionService secondInstanceConnectionService) {
        this.secondInstanceConnectionService = secondInstanceConnectionService;
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {

            final Supplier<RemoteInvoker> remoteInvokerSupplier = mock(Supplier.class);
            bind(RemoteInvoker.class).toProvider(remoteInvokerSupplier::get);
            bind(new TypeLiteral<Supplier<RemoteInvoker>>(){}).toInstance(remoteInvokerSupplier);

            bind(ZContext.class).asEagerSingleton();

            bind(MockInstanceDiscoveryService.class).toInstance(spy(MockInstanceDiscoveryService.class));
            bind(InstanceDiscoveryService.class).to(MockInstanceDiscoveryService.class);

            install(new PrivateModule() {
                @Override
                protected void configure() {

                    expose(Key.get(InstanceConnectionService.class, named(BIND_URL_FIRST)));

                    bind(InstanceConnectionService.class)
                        .annotatedWith(named(BIND_URL_FIRST))
                        .to(JeroMQInstanceConnectionService.class)
                        .asEagerSingleton();

                    bind(String.class)
                        .annotatedWith(named(BIND_ADDRESS))
                        .toInstance(BIND_URL_FIRST);

                    bind(InstanceId.class).toInstance(new InstanceId());

                }
            });

            install(new PrivateModule() {
                @Override
                protected void configure() {

                    expose(Key.get(InstanceConnectionService.class, named(BIND_URL_SECOND)));

                    bind(InstanceConnectionService.class)
                        .annotatedWith(named(BIND_URL_SECOND))
                        .to(JeroMQInstanceConnectionService.class)
                        .asEagerSingleton();

                    bind(String.class)
                        .annotatedWith(named(BIND_ADDRESS))
                        .toInstance(BIND_URL_SECOND);

                    bind(InstanceId.class).toInstance(new InstanceId());

                }
            });

        }
    }

}
