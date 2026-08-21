package dev.getelements.elements.rt.remote;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.InstanceMetadata;
import dev.getelements.elements.rt.InstanceMetadataContext;
import dev.getelements.elements.rt.remote.InstanceConnectionService.InstanceConnection;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.*;
import static dev.getelements.elements.sdk.cluster.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.sdk.cluster.id.NodeId.forInstanceAndApplication;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.collections.Lists.newArrayList;

@Guice(modules = SimpleRemoteInvokerRegistryTest.Module.class)
public class SimpleRemoteInvokerRegistryTest {

    private RemoteInvokerRegistry remoteInvokerRegistry;

    private MockInstanceConnectionService instanceConnectionService;

    @BeforeMethod
    private void resetMocks() {
        getInstanceConnectionService().resetInternal();
        reset(getInstanceConnectionService());
    }

    @Test
    public void testStartAndLoadInitialConnections() throws Exception {

        // Setup Mocks

        final List<InstanceConnection> mockActiveConnections = unmodifiableList(asList(
            mock(InstanceConnection.class),
            mock(InstanceConnection.class),
            mock(InstanceConnection.class)
        ));

        final List<ApplicationId> mockApplicationIds = newArrayList(
            randomApplicationId(), randomApplicationId(), randomApplicationId(), randomApplicationId()
        );


        final CountDownLatch latch = new CountDownLatch(mockActiveConnections.size() * mockApplicationIds.size());

        final Random random = new Random();

        final List<RemoteInvoker> mockRemoteInvokerList = new ArrayList<>();

        final Set<NodeId> mockNodeIds = new HashSet<>();
        final Map<NodeId, RemoteInvoker> mockRemoteInvokerMap = new HashMap<>();

        final Set<InstanceId> mockInstanceIds = new HashSet<>();

        final Map<InstanceId, Double> mockLoadMap = new HashMap<>();
        final Map<InstanceId, InstanceConnection> mockInstanceConnectionMap = new HashMap<>();

        mockActiveConnections.forEach(mockInstanceConnection -> {

            final InstanceId instanceId = randomInstanceId();

            final Double load = random.nextDouble();
            final InstanceMetadataContext mockInstanceMetadataContext = mock(InstanceMetadataContext.class);

            final Set<NodeId> nodeIdSet = unmodifiableSet(mockApplicationIds
                .stream()
                .map(a -> forInstanceAndApplication(instanceId, a))
                .collect(toSet()));

            mockNodeIds.addAll(nodeIdSet);
            mockInstanceIds.add(instanceId);

            when(mockInstanceConnection.getInstanceId()).thenReturn(instanceId);
            when(mockInstanceConnection.getInstanceMetadataContext()).thenReturn(mockInstanceMetadataContext);

            when(mockInstanceMetadataContext.getInstanceMetadataAsync(any(), any())).thenAnswer(i -> {
                final Consumer<InstanceMetadata> instanceMetadataConsumer = i.getArgument(0);
                final Consumer<Throwable> failure = i.getArgument(0);
                final InstanceMetadata instanceMetadata = new InstanceMetadata();
                instanceMetadata.setQuality(load);
                instanceMetadata.setNodeIds(nodeIdSet);
                instanceMetadataConsumer.accept(instanceMetadata);
                return mock(AsyncOperation.class);
            });

            when(mockInstanceMetadataContext.getNodeIds()).thenReturn(nodeIdSet);
            when(mockInstanceMetadataContext.getInstanceQuality()).thenReturn(load);

            // Per the new dial-per-invocation transport, one RemoteInvoker is shared across every NodeId hosted by
            // the same InstanceConnection - there is no more per-node address/route negotiation.
            final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
            mockRemoteInvokerList.add(mockRemoteInvoker);

            when(mockInstanceConnection.getRemoteInvoker()).thenAnswer(i -> {
                latch.countDown();
                return mockRemoteInvoker;
            });

            nodeIdSet.forEach(nid -> mockRemoteInvokerMap.put(nid, mockRemoteInvoker));

            mockLoadMap.put(instanceId, load);
            mockInstanceConnectionMap.put(instanceId, mockInstanceConnection);

        });

        when(getInstanceConnectionService().getActiveConnections()).thenReturn(mockActiveConnections);

        // Start and Stop Service
        getRemoteInvokerRegistry().start();

        latch.await();

        // Determines the best instance based on the mocked load values.
        final InstanceId best = mockActiveConnections
            .stream()
            .map(c -> c.getInstanceId())
            .sorted((i0, i1) -> {
                final double quality0 = mockLoadMap.get(i0);
                final double quality1 = mockLoadMap.get(i1);
                return Double.compare(quality1, quality0);
            }).findFirst().get();

        // Verify that the best instance is returned for each application when requesting any remote invoker, and
        // that it is the exact invoker minted for that instance's connection.
        mockApplicationIds.forEach(a -> {
            final var nodeId = forInstanceAndApplication(best, a);
            final var expected = mockRemoteInvokerMap.get(nodeId);
            final var remoteInvoker = getRemoteInvokerRegistry().getBestRemoteInvoker(a);
            assertSame(remoteInvoker, expected);
        });

        // Verify that the requested remote invoker was connected to the correct node id
        mockInstanceIds.forEach(iid -> mockApplicationIds.forEach(aid -> getRemoteInvokerRegistry().getAllRemoteInvokers(aid).forEach(ri -> {

            final NodeId nodeId = forInstanceAndApplication(iid, aid);
            final var mockRemoteInvoker = mockRemoteInvokerMap.get(nodeId);
            if (ri != mockRemoteInvoker) return;

            assertSame(ri, mockRemoteInvoker);

        })));

        // Verifies that the requested remote invoker for the correct NodeId was actually connected to the correct
        // node id
        mockInstanceIds.forEach(iid -> mockApplicationIds.forEach(aid -> {
            final NodeId nodeId = forInstanceAndApplication(iid, aid);
            final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
            assertSame(remoteInvoker, mockRemoteInvokerMap.get(nodeId));
        }));

        mockNodeIds.forEach(getRemoteInvokerRegistry()::getRemoteInvoker);

        // Stops the service so we can finish verification
        getRemoteInvokerRegistry().stop();

        // Verify Mocks
        verify(getInstanceConnectionService(), atLeastOnce()).getActiveConnections();
        verify(getInstanceConnectionService(), times(1)).subscribeToConnect(any());
        verify(getInstanceConnectionService(), times(1)).subscribeToDisconnect(any());

        mockActiveConnections.forEach(c -> {

            c.getInstanceId();
            verify(c, atLeastOnce()).getInstanceId();
            verify(c, atLeastOnce()).getInstanceMetadataContext();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getInstanceMetadataAsync(any(), any());
            verify(c, times(mockApplicationIds.size())).getRemoteInvoker();

        });

        // A full registry stop clears every NodeId entry individually (no de-duplication), so a shared
        // per-connection invoker is stopped once per NodeId hosted by that connection.
        mockRemoteInvokerList.forEach(ri -> verify(ri, times(mockApplicationIds.size())).stop());

    }

    @Test(dependsOnMethods = "testStartAndLoadInitialConnections")
    public void testEventDrivenConnectionAdd() throws Exception {

        final List<InstanceConnection> mockActiveConnections = synchronizedList(new ArrayList<>());

        final List<ApplicationId> mockApplicationIds = newArrayList(
            randomApplicationId(), randomApplicationId(), randomApplicationId(), randomApplicationId()
        );

        final CountDownLatch latch = new CountDownLatch(5 * mockApplicationIds.size());

        final Random random = new Random();

        final List<RemoteInvoker> mockRemoteInvokerList = new ArrayList<>();

        final Set<NodeId> mockNodeIds = new HashSet<>();
        final Map<NodeId, RemoteInvoker> mockRemoteInvokerMap = new HashMap<>();

        final Set<InstanceId> mockInstanceIds = new HashSet<>();

        final Map<InstanceId, Double> mockLoadMap = new HashMap<>();
        final Map<InstanceId, InstanceConnection> mockInstanceConnectionMap = new HashMap<>();

        when(getInstanceConnectionService().getActiveConnections()).thenReturn(mockActiveConnections);

        // Start and Stop Service
        getRemoteInvokerRegistry().start();

        mockActiveConnections.addAll(IntStream.range(0, 5).mapToObj(i -> {

            final InstanceId instanceId = randomInstanceId();

            final Double load = random.nextDouble();
            final InstanceConnection mockInstanceConnection = mock(InstanceConnection.class);
            final InstanceMetadataContext mockInstanceMetadataContext = mock(InstanceMetadataContext.class);

            final Set<NodeId> nodeIdSet = unmodifiableSet(mockApplicationIds
                    .stream()
                    .map(a -> forInstanceAndApplication(instanceId, a))
                    .collect(toSet()));

            mockNodeIds.addAll(nodeIdSet);
            mockInstanceIds.add(instanceId);

            when(mockInstanceConnection.getInstanceId()).thenReturn(instanceId);
            when(mockInstanceConnection.getInstanceMetadataContext()).thenReturn(mockInstanceMetadataContext);

            when(mockInstanceMetadataContext.getInstanceMetadataAsync(any(), any())).thenAnswer(inv -> {
                final Consumer<InstanceMetadata> instanceMetadataConsumer = inv.getArgument(0);
                final Consumer<Throwable> failure = inv.getArgument(0);
                final InstanceMetadata instanceMetadata = new InstanceMetadata();
                instanceMetadata.setQuality(load);
                instanceMetadata.setNodeIds(nodeIdSet);
                instanceMetadataConsumer.accept(instanceMetadata);
                return mock(AsyncOperation.class);
            });

            when(mockInstanceMetadataContext.getNodeIds()).thenReturn(nodeIdSet);
            when(mockInstanceMetadataContext.getInstanceQuality()).thenReturn(load);

            final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
            mockRemoteInvokerList.add(mockRemoteInvoker);

            when(mockInstanceConnection.getRemoteInvoker()).thenAnswer(inv -> {
                latch.countDown();
                return mockRemoteInvoker;
            });

            nodeIdSet.forEach(nid -> mockRemoteInvokerMap.put(nid, mockRemoteInvoker));

            mockLoadMap.put(instanceId, load);
            mockInstanceConnectionMap.put(instanceId, mockInstanceConnection);

            return mockInstanceConnection;

        }).sorted((i0, i1) -> {
            final double quality0 = i0.getInstanceMetadataContext().getInstanceQuality();
            final double quality1 = i1.getInstanceMetadataContext().getInstanceQuality();
            return Double.compare(quality1, quality0);
        }).collect(toList()));

        // Determines the best instance based on the mocked load values.
        final InstanceId best = mockActiveConnections.get(0).getInstanceId();

        // Drives all events that will be published to the service
        mockActiveConnections.forEach(getInstanceConnectionService().getOnConnectPublisher()::publish);

        // Waits to make sure each remote invoker was minted.
        latch.await();

        // Verify that the best instance is returned for each application when requesting any remote invoker.
        mockApplicationIds.forEach(a -> {
            final var nodeId = forInstanceAndApplication(best, a);
            final var expected = mockRemoteInvokerMap.get(nodeId);
            final var remoteInvoker = getRemoteInvokerRegistry().getBestRemoteInvoker(a);
            assertSame(remoteInvoker, expected);
        });

        // Verify that the requested remote invoker was connected to the correct node id
        mockInstanceIds.forEach(iid -> mockApplicationIds.forEach(aid -> getRemoteInvokerRegistry().getAllRemoteInvokers(aid).forEach(ri -> {

            final var nodeId = forInstanceAndApplication(iid, aid);
            final var mockRemoteInvoker = mockRemoteInvokerMap.get(nodeId);
            if (ri != mockRemoteInvoker) return;

            assertSame(ri, mockRemoteInvoker);

        })));

        // Verifies that the requested remote invoker for the correct NodeId was actually connected to the correct
        // node id
        mockInstanceIds.forEach(iid -> mockApplicationIds.forEach(aid -> {
            final NodeId nodeId = forInstanceAndApplication(iid, aid);
            final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
            assertSame(remoteInvoker, mockRemoteInvokerMap.get(nodeId));
        }));

        // Stops the service so we can finish verification
        getRemoteInvokerRegistry().stop();

        // Verify Mocks
        verify(getInstanceConnectionService(), atLeastOnce()).getActiveConnections();
        verify(getInstanceConnectionService(), times(1)).subscribeToConnect(any());
        verify(getInstanceConnectionService(), times(1)).subscribeToDisconnect(any());

        mockActiveConnections.forEach(c -> {

            c.getInstanceId();
            verify(c, atLeastOnce()).getInstanceId();
            verify(c, atLeastOnce()).getInstanceMetadataContext();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getInstanceMetadataAsync(any(), any());
            verify(c, times(mockApplicationIds.size())).getRemoteInvoker();

        });

        // A full registry stop clears every NodeId entry individually (no de-duplication), so a shared
        // per-connection invoker is stopped once per NodeId hosted by that connection.
        mockRemoteInvokerList.forEach(ri -> verify(ri, times(mockApplicationIds.size())).stop());

    }

    @Test(dependsOnMethods = "testEventDrivenConnectionAdd")
    public void testEventDrivenConnectionRemove() throws Exception {

        final List<InstanceConnection> mockActiveConnections = synchronizedList(new ArrayList<>());

        final List<ApplicationId> mockApplicationIds = newArrayList(
                randomApplicationId(), randomApplicationId(), randomApplicationId(), randomApplicationId()
        );

        final CountDownLatch latch = new CountDownLatch(5 * mockApplicationIds.size());

        final Random random = new Random();

        final List<RemoteInvoker> mockRemoteInvokerList = new ArrayList<>();

        final Set<NodeId> mockNodeIds = new HashSet<>();
        final Map<NodeId, RemoteInvoker> mockRemoteInvokerMap = new HashMap<>();

        final Set<InstanceId> mockInstanceIds = new HashSet<>();

        final Map<InstanceId, Double> mockLoadMap = new HashMap<>();
        final Map<InstanceId, InstanceConnection> mockInstanceConnectionMap = new HashMap<>();

        mockActiveConnections.addAll(IntStream.range(0, 5).mapToObj(i -> {

            final InstanceId instanceId = randomInstanceId();

            final Double load = random.nextDouble();
            final InstanceConnection mockInstanceConnection = mock(InstanceConnection.class);
            final InstanceMetadataContext mockInstanceMetadataContext = mock(InstanceMetadataContext.class);

            final Set<NodeId> nodeIdSet = unmodifiableSet(mockApplicationIds
                    .stream()
                    .map(a -> forInstanceAndApplication(instanceId, a))
                    .collect(toSet()));

            mockNodeIds.addAll(nodeIdSet);
            mockInstanceIds.add(instanceId);

            when(mockInstanceConnection.getInstanceId()).thenReturn(instanceId);
            when(mockInstanceConnection.getInstanceMetadataContext()).thenReturn(mockInstanceMetadataContext);

            when(mockInstanceMetadataContext.getInstanceMetadataAsync(any(), any())).thenAnswer(inv -> {
                final Consumer<InstanceMetadata> instanceMetadataConsumer = inv.getArgument(0);
                final Consumer<Throwable> failure = inv.getArgument(0);
                final InstanceMetadata instanceMetadata = new InstanceMetadata();
                instanceMetadata.setQuality(load);
                instanceMetadata.setNodeIds(nodeIdSet);
                instanceMetadataConsumer.accept(instanceMetadata);
                return mock(AsyncOperation.class);
            });

            when(mockInstanceMetadataContext.getNodeIds()).thenReturn(nodeIdSet);
            when(mockInstanceMetadataContext.getInstanceQuality()).thenReturn(load);

            final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
            mockRemoteInvokerList.add(mockRemoteInvoker);

            when(mockInstanceConnection.getRemoteInvoker()).thenAnswer(inv -> {
                latch.countDown();
                return mockRemoteInvoker;
            });

            nodeIdSet.forEach(nid -> mockRemoteInvokerMap.put(nid, mockRemoteInvoker));

            mockLoadMap.put(instanceId, load);
            mockInstanceConnectionMap.put(instanceId, mockInstanceConnection);

            return mockInstanceConnection;

        }).sorted((i0, i1) -> {
            final double quality0 = i0.getInstanceMetadataContext().getInstanceQuality();
            final double quality1 = i1.getInstanceMetadataContext().getInstanceQuality();
            return Double.compare(quality1, quality0);
        }).collect(toList()));

        when(getInstanceConnectionService().getActiveConnections()).thenReturn(mockActiveConnections);

        // Start and Stop Service
        getRemoteInvokerRegistry().start();

        // Removes one using an event after the list has been built internally

        final InstanceConnection removed = mockActiveConnections.remove(0);
        getInstanceConnectionService().getOnDisconnectPublisher().publish(removed);

        // Determines the best instance based on the mocked load values.
        final InstanceId best = mockActiveConnections.get(0).getInstanceId();

        // Verify that the best instance is returned for each application when requesting any remote invoker.
        mockApplicationIds.forEach(a -> {
            final var nodeId = forInstanceAndApplication(best, a);
            final var expected = mockRemoteInvokerMap.get(nodeId);
            final var remoteInvoker = getRemoteInvokerRegistry().getBestRemoteInvoker(a);
            assertSame(remoteInvoker, expected);
        });

        // Verify that the requested remote invoker was connected to the correct node id
        mockInstanceIds.forEach(iid -> mockApplicationIds.forEach(aid -> getRemoteInvokerRegistry().getAllRemoteInvokers(aid).forEach(ri -> {

            final var nodeId = forInstanceAndApplication(iid, aid);
            final var mockRemoteInvoker = mockRemoteInvokerMap.get(nodeId);
            final var remoteInvoker = ri;
            if (remoteInvoker != mockRemoteInvoker) return;

            assertSame(remoteInvoker, mockRemoteInvoker);

        })));

        // Verifies that the requested remote invoker for the correct NodeId was actually connected to the correct
        // node id
        mockInstanceIds.forEach(iid -> mockApplicationIds.forEach(aid -> {

            final NodeId nodeId = forInstanceAndApplication(iid, aid);

            if (nodeId.equals(forInstanceAndApplication(removed.getInstanceId(), aid))) {
                final RemoteInvoker remoteInvoker = mockRemoteInvokerMap.get(nodeId);
                assertNotNull(remoteInvoker);
                verify(remoteInvoker, times(1)).stop();
            } else {
                final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
                assertSame(remoteInvoker, mockRemoteInvokerMap.get(nodeId));
            }


        }));

        // Stops the service so we can finish verification
        getRemoteInvokerRegistry().stop();

        // Verify Mocks
        verify(getInstanceConnectionService(), atLeastOnce()).getActiveConnections();
        verify(getInstanceConnectionService(), times(1)).subscribeToConnect(any());
        verify(getInstanceConnectionService(), times(1)).subscribeToDisconnect(any());

        mockActiveConnections.forEach(c -> {

            c.getInstanceId();
            verify(c, atLeastOnce()).getInstanceId();
            verify(c, atLeastOnce()).getInstanceMetadataContext();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getInstanceMetadataAsync(any(), any());
            verify(c, times(mockApplicationIds.size())).getRemoteInvoker();

        });

        // The removed connection's invoker was already stopped exactly once by the mid-test disconnect (a
        // de-duplicated remove). The remaining connections' invokers are stopped once per NodeId they host, since
        // the final full registry stop clears every NodeId entry individually (no de-duplication).
        final var removedInvoker = mockRemoteInvokerMap.get(forInstanceAndApplication(removed.getInstanceId(), mockApplicationIds.get(0)));

        mockRemoteInvokerList.forEach(ri -> {
            final int expectedStops = ri == removedInvoker ? 1 : mockApplicationIds.size();
            verify(ri, times(expectedStops)).stop();
        });

    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

    public MockInstanceConnectionService getInstanceConnectionService() {
        return instanceConnectionService;
    }

    @Inject
    public void setInstanceConnectionService(MockInstanceConnectionService instanceConnectionService) {
        this.instanceConnectionService = instanceConnectionService;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final InstanceId instanceId = randomInstanceId();
            bind(InstanceId.class).toInstance(instanceId);

            bind(RemoteInvokerRegistry.class).to(SimpleRemoteInvokerRegistry.class).asEagerSingleton();

            bind(MockInstanceConnectionService.class).toInstance(spy(MockInstanceConnectionService.class));
            bind(InstanceConnectionService.class).to(MockInstanceConnectionService.class);

            bind(Long.class)
                .annotatedWith(named(REFRESH_RATE_SECONDS))
                .toInstance(DEFAULT_REFRESH_RATE);

            bind(Long.class)
                .annotatedWith(named(REFRESH_TIMEOUT_SECONDS))
                .toInstance(DEFAULT_REFRESH_TIMEOUT);

            bind(Long.class)
                .annotatedWith(named(TOTAL_REFRESH_TIMEOUT_SECONDS))
                .toInstance(DEFAULT_TOTAL_REFRESH_TIMEOUT);

        }

    }

}
