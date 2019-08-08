package com.namazustudios.socialengine.rt.remote.jeromq.guice.com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import com.namazustudios.socialengine.rt.remote.InstanceStatus;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlClient;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.BIND_ADDRESS;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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

    private final List<UUID> mockApplicationUUIDs = unmodifiableList(asList(
        randomUUID(), randomUUID(), randomUUID(), randomUUID()
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

        for (final UUID mockApplicationUuid : mockApplicationUUIDs) {
            runEchoServer(getFirstInstanceConnectionService(), mockApplicationUuid);
            runEchoServer(getSecondInstanceConnectionService(), mockApplicationUuid);
        }

        assertNodeStatusIsCorrect(getFirstInstanceConnectionService());
        assertNodeStatusIsCorrect(getSecondInstanceConnectionService());

    }

    private void runEchoServer(final InstanceConnectionService instanceConnectionService, final UUID applicationUuuid) {

        final CountDownLatch latch = new CountDownLatch(1);

        final Supplier<InstanceBinding> instanceBindingSupplier = () -> {
            final NodeId nodeId = new NodeId(instanceConnectionService.getInstanceId(), applicationUuuid);
            return instanceConnectionService.openBinding(nodeId);
        };

        executorService.submit(() -> {
            try (final JeroMQEchoServer svr = new JeroMQEchoServer(getzContext(), instanceBindingSupplier)) {
                latch.countDown();
                svr.run();
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

            final Set<NodeId> mockNodeIdSet = mockApplicationUUIDs
                    .stream()
                    .map(aid -> new NodeId(instanceConnectionService.getInstanceId(), aid))
                    .collect(toSet());

            assertEquals(instanceStatusSet, mockNodeIdSet);
            assertEquals(instanceStatus.getInstanceId(), instanceConnectionService.getInstanceId());

        }

    }

    @Test(dependsOnMethods = "testOpenBindingsWithEchoServers")
    public void testAddConnections() {
        getMockInstanceDiscoveryService().addHosts(BIND_URL_FIRST, BIND_URL_SECOND);
    }

    @Test(dependsOnMethods = {
            "testStartTwiceThrowsFirst",
            "testStartTwiceThrowsSecond",
            "testOpenBindingsWithEchoServers",
            "testAddConnections"
    })
    public void testStop() {
        getFirstInstanceConnectionService().stop();
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
