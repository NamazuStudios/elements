package com.namazustudios.socialengine.rt.remote;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceConnection;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.collections.Lists.newArrayList;

@Guice(modules = SimpleRemoteInvokerRegistryTest.Module.class)
public class SimpleRemoteInvokerRegistryTest {

    private RemoteInvokerRegistry remoteInvokerRegistry;

    private Supplier<RemoteInvoker> remoteInvokerSupplier;

    private MockInstanceConnectionService instanceConnectionService;

    @BeforeMethod
    private void resetMocks() {
        getInstanceConnectionService().resetInternal();
        reset(getInstanceConnectionService());
        reset(getRemoteInvokerSupplier());
    }

    @Test
    public void testStartAndLoadInitialConnections() throws Exception {

        // Setup Mocks

        final String prefix = "test+protocol://";

        final List<InstanceConnection> mockActiveConnections = unmodifiableList(asList(
            mock(InstanceConnection.class),
            mock(InstanceConnection.class),
            mock(InstanceConnection.class)
        ));

        final List<UUID> mockApplicationUUIDs = newArrayList(
            randomUUID(), randomUUID(), randomUUID(), randomUUID()
        );


        final CountDownLatch latch = new CountDownLatch(mockActiveConnections.size() * mockApplicationUUIDs.size());

        final Random random = new Random();

        final List<RemoteInvoker> mockRemoteInvokerList = new ArrayList<>();

        final Set<NodeId> mockNodeIds = new HashSet<>();
        final Map<NodeId, RemoteInvoker> mockRemoteInvokerMap = new HashMap<>();

        final Set<InstanceId> mockInstanceIds = new HashSet<>();

        final Map<InstanceId, Double> mockLoadMap = new HashMap<>();
        final Map<InstanceId, InstanceConnection> mockInstanceConnectionMap = new HashMap<>();

        mockActiveConnections.forEach(mockInstanceConnection -> {

            final InstanceId instanceId = new InstanceId();

            final Double load = random.nextDouble();
            final InstanceMetadataContext mockInstanceMetadataContext = mock(InstanceMetadataContext.class);

            final Set<NodeId> nodeIdSet = unmodifiableSet(mockApplicationUUIDs
                .stream()
                .map(a -> new NodeId(instanceId, a))
                .collect(toSet()));

            mockNodeIds.addAll(nodeIdSet);
            mockInstanceIds.add(instanceId);

            when(mockInstanceConnection.getInstanceId()).thenReturn(instanceId);
            when(mockInstanceConnection.getInstanceMetadataContext()).thenReturn(mockInstanceMetadataContext);

            when(mockInstanceMetadataContext.getNodeIds()).thenReturn(nodeIdSet);
            when(mockInstanceMetadataContext.getInstanceLoad()).thenReturn(load);
            nodeIdSet.forEach(nid -> when(mockInstanceConnection.openRouteToNode(eq(nid))).thenReturn(prefix + nid.asString()));

            mockLoadMap.put(instanceId, load);
            mockInstanceConnectionMap.put(instanceId, mockInstanceConnection);

        });

        when(getRemoteInvokerSupplier().get()).thenAnswer(i -> {

            final RemoteInvoker invoker = mock(RemoteInvoker.class);

            doAnswer(i0 -> {
                final String address = i0.getArgument(0);
                final String nodeIdString = address.substring(prefix.length());
                final NodeId nodeId = new NodeId(nodeIdString);
                mockRemoteInvokerMap.put(nodeId, invoker);
                assertTrue(mockNodeIds.contains(nodeId), "Connected nodes does not contain: " + nodeId);
                latch.countDown();
                return null;
            }).when(invoker).start(anyString());

            mockRemoteInvokerList.add(invoker);
            return invoker;

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
                final double load0 = mockLoadMap.get(i0);
                final double load1 = mockLoadMap.get(i1);
                return Double.compare(load0, load1);
            }).findFirst().get();

        // Verify that the best instance is returned for each application when requesting any remote invoker.  Since
        // the remote invoker only knows based on connect address, we ensure that the remotes invoker was connected
        // to the correct address based on our own mock addressing scheme.
        mockApplicationUUIDs.forEach(a -> {
            final NodeId nodeId = new NodeId(best, a);
            final String addr = prefix + nodeId.asString();
            final PriorityRemoteInvoker priorityRemoteInvoker = (PriorityRemoteInvoker) getRemoteInvokerRegistry().getBestRemoteInvoker(a);
            verify(priorityRemoteInvoker.getDelegate(), times(1)).start(eq(addr));
        });

        // Verify that the requested remote invoker was connected to the correct node id
        mockInstanceIds.forEach(iid -> mockApplicationUUIDs.forEach(aid -> getRemoteInvokerRegistry().getAllRemoteInvokers(aid).forEach(ri -> {

            final NodeId nodeId = new NodeId(iid, aid);
            final RemoteInvoker mockRemoteInvoker = mockRemoteInvokerMap.get(nodeId);
            final PriorityRemoteInvoker priorityRemoteInvoker = (PriorityRemoteInvoker)ri;
            if (priorityRemoteInvoker.getDelegate() != mockRemoteInvoker) return;

            final String addr = prefix + nodeId.asString();
            verify(mockRemoteInvoker, times(1)).start(eq(addr));

        })));

        // Verifies that the requested remote invoker for the correct NodeId was actually connected tot he correct node id
        mockInstanceIds.forEach(iid -> mockApplicationUUIDs.forEach(aid -> {
            final NodeId nodeId = new NodeId(iid, aid);
            final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
            final String addr = prefix + nodeId.asString();
            verify(remoteInvoker, times(1)).start(eq(addr));
        }));

        mockNodeIds.forEach(getRemoteInvokerRegistry()::getRemoteInvoker);

        // Stops the service so we can finish verification
        getRemoteInvokerRegistry().stop();

        // Verify Mocks
        verify(getInstanceConnectionService(), atLeastOnce()).getActiveConnections();
        verify(getInstanceConnectionService(), times(1)).subscribeToConnect(any());
        verify(getInstanceConnectionService(), times(1)).subscribeToDisconnect(any());

        mockActiveConnections.forEach(c -> {

            final InstanceId instanceId = c.getInstanceId();

            verify(c, atLeastOnce()).getInstanceId();
            verify(c, atLeastOnce()).getInstanceMetadataContext();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getNodeIds();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getInstanceLoad();

            mockApplicationUUIDs.forEach(a -> {
                final NodeId nodeId = new NodeId(instanceId, a);
                verify(c, times(1)).openRouteToNode(eq(nodeId));
            });

        });

        mockRemoteInvokerList.forEach(ri -> verify(ri, times(1)).stop());

    }

    @Test(dependsOnMethods = "testStartAndLoadInitialConnections")
    public void testEventDrivenConnectionAdd() throws Exception {

        final String prefix = "test+protocol://";

        final List<InstanceConnection> mockActiveConnections = synchronizedList(new ArrayList<>());

        final List<UUID> mockApplicationUUIDs = newArrayList(
                randomUUID(), randomUUID(), randomUUID(), randomUUID()
        );

        final CountDownLatch latch = new CountDownLatch(5 * mockApplicationUUIDs.size());

        final Random random = new Random();

        final List<RemoteInvoker> mockRemoteInvokerList = new ArrayList<>();

        final Set<NodeId> mockNodeIds = new HashSet<>();
        final Map<NodeId, RemoteInvoker> mockRemoteInvokerMap = new HashMap<>();

        final Set<InstanceId> mockInstanceIds = new HashSet<>();

        final Map<InstanceId, Double> mockLoadMap = new HashMap<>();
        final Map<InstanceId, InstanceConnection> mockInstanceConnectionMap = new HashMap<>();

        when(getRemoteInvokerSupplier().get()).thenAnswer(i -> {

            final RemoteInvoker invoker = mock(RemoteInvoker.class);

            doAnswer(i0 -> {
                final String address = i0.getArgument(0);
                final String nodeIdString = address.substring(prefix.length());
                final NodeId nodeId = new NodeId(nodeIdString);
                mockRemoteInvokerMap.put(nodeId, invoker);
                latch.countDown();
                assertTrue(mockNodeIds.contains(nodeId), "Connected nodes does not contain: " + nodeId);
                return null;
            }).when(invoker).start(anyString());

            mockRemoteInvokerList.add(invoker);
            return invoker;

        });

        when(getInstanceConnectionService().getActiveConnections()).thenReturn(mockActiveConnections);

        // Start and Stop Service
        getRemoteInvokerRegistry().start();

        mockActiveConnections.addAll(IntStream.range(0, 5).mapToObj(i -> {

            final InstanceId instanceId = new InstanceId();

            final Double load = random.nextDouble();
            final InstanceConnection mockInstanceConnection = mock(InstanceConnection.class);
            final InstanceMetadataContext mockInstanceMetadataContext = mock(InstanceMetadataContext.class);

            final Set<NodeId> nodeIdSet = unmodifiableSet(mockApplicationUUIDs
                    .stream()
                    .map(a -> new NodeId(instanceId, a))
                    .collect(toSet()));

            mockNodeIds.addAll(nodeIdSet);
            mockInstanceIds.add(instanceId);

            when(mockInstanceConnection.getInstanceId()).thenReturn(instanceId);
            when(mockInstanceConnection.getInstanceMetadataContext()).thenReturn(mockInstanceMetadataContext);

            when(mockInstanceMetadataContext.getNodeIds()).thenReturn(nodeIdSet);
            when(mockInstanceMetadataContext.getInstanceLoad()).thenReturn(load);
            nodeIdSet.forEach(nid -> when(mockInstanceConnection.openRouteToNode(eq(nid))).thenReturn(prefix + nid.asString()));

            mockLoadMap.put(instanceId, load);
            mockInstanceConnectionMap.put(instanceId, mockInstanceConnection);

            return mockInstanceConnection;

        }).sorted((i0, i1) -> {
            final double load0 = i0.getInstanceMetadataContext().getInstanceLoad();
            final double load1 = i1.getInstanceMetadataContext().getInstanceLoad();
            return Double.compare(load0, load1);
        }).collect(toList()));

        // Determines the best instance based on the mocked load values.
        final InstanceId best = mockActiveConnections.get(0).getInstanceId();

        // Drives all events that will be published to the service
        mockActiveConnections.forEach(getInstanceConnectionService().getOnConnectPublisher()::publish);

        // Waits to make sure each remote invoker was started.
        latch.await();

        // Verify that the best instance is returned for each application when requesting any remote invoker.  Since
        // the remote invoker only knows based on connect address, we ensure that the remotes invoker was connected
        // to the correct address based on our own mock addressing scheme.
        mockApplicationUUIDs.forEach(a -> {
            final NodeId nodeId = new NodeId(best, a);
            final String addr = prefix + nodeId.asString();
            final PriorityRemoteInvoker priorityRemoteInvoker = (PriorityRemoteInvoker) getRemoteInvokerRegistry().getBestRemoteInvoker(a);
            verify(priorityRemoteInvoker.getDelegate(), times(1)).start(eq(addr));
        });

        // Verify that the requested remote invoker was connected to the correct node id
        mockInstanceIds.forEach(iid -> mockApplicationUUIDs.forEach(aid -> getRemoteInvokerRegistry().getAllRemoteInvokers(aid).forEach(ri -> {

            final NodeId nodeId = new NodeId(iid, aid);
            final RemoteInvoker mockRemoteInvoker = mockRemoteInvokerMap.get(nodeId);
            final PriorityRemoteInvoker priorityRemoteInvoker = (PriorityRemoteInvoker)ri;
            if (priorityRemoteInvoker.getDelegate() != mockRemoteInvoker) return;

            final String addr = prefix + nodeId.asString();
            verify(mockRemoteInvoker, times(1)).start(eq(addr));

        })));

        // Verifies that the requested remote invoker for the correct NodeId was actually connected tot he correct node id
        mockInstanceIds.forEach(iid -> mockApplicationUUIDs.forEach(aid -> {
            final NodeId nodeId = new NodeId(iid, aid);
            final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
            final String addr = prefix + nodeId.asString();
            verify(remoteInvoker, times(1)).start(eq(addr));
        }));

        // Stops the service so we can finish verification
        getRemoteInvokerRegistry().stop();

        // Verify Mocks
        verify(getInstanceConnectionService(), atLeastOnce()).getActiveConnections();
        verify(getInstanceConnectionService(), times(1)).subscribeToConnect(any());
        verify(getInstanceConnectionService(), times(1)).subscribeToDisconnect(any());

        mockActiveConnections.forEach(c -> {

            final InstanceId instanceId = c.getInstanceId();

            verify(c, atLeastOnce()).getInstanceId();
            verify(c, atLeastOnce()).getInstanceMetadataContext();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getNodeIds();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getInstanceLoad();

            mockApplicationUUIDs.forEach(a -> {
                final NodeId nodeId = new NodeId(instanceId, a);
                verify(c, times(1)).openRouteToNode(eq(nodeId));
            });

        });

        mockRemoteInvokerList.forEach(ri -> verify(ri, times(1)).stop());

    }

    @Test(dependsOnMethods = "testEventDrivenConnectionAdd")
    public void testEventDrivenConnectionRemove() throws Exception {

        final String prefix = "test+protocol://";

        final List<InstanceConnection> mockActiveConnections = synchronizedList(new ArrayList<>());

        final List<UUID> mockApplicationUUIDs = newArrayList(
                randomUUID(), randomUUID(), randomUUID(), randomUUID()
        );

        final CountDownLatch latch = new CountDownLatch(5 * mockApplicationUUIDs.size());

        final Random random = new Random();

        final List<RemoteInvoker> mockRemoteInvokerList = new ArrayList<>();

        final Set<NodeId> mockNodeIds = new HashSet<>();
        final Map<NodeId, RemoteInvoker> mockRemoteInvokerMap = new HashMap<>();

        final Set<InstanceId> mockInstanceIds = new HashSet<>();

        final Map<InstanceId, Double> mockLoadMap = new HashMap<>();
        final Map<InstanceId, InstanceConnection> mockInstanceConnectionMap = new HashMap<>();

        mockActiveConnections.addAll(IntStream.range(0, 5).mapToObj(i -> {

            final InstanceId instanceId = new InstanceId();

            final Double load = random.nextDouble();
            final InstanceConnection mockInstanceConnection = mock(InstanceConnection.class);
            final InstanceMetadataContext mockInstanceMetadataContext = mock(InstanceMetadataContext.class);

            final Set<NodeId> nodeIdSet = unmodifiableSet(mockApplicationUUIDs
                    .stream()
                    .map(a -> new NodeId(instanceId, a))
                    .collect(toSet()));

            mockNodeIds.addAll(nodeIdSet);
            mockInstanceIds.add(instanceId);

            when(mockInstanceConnection.getInstanceId()).thenReturn(instanceId);
            when(mockInstanceConnection.getInstanceMetadataContext()).thenReturn(mockInstanceMetadataContext);

            when(mockInstanceMetadataContext.getNodeIds()).thenReturn(nodeIdSet);
            when(mockInstanceMetadataContext.getInstanceLoad()).thenReturn(load);
            nodeIdSet.forEach(nid -> when(mockInstanceConnection.openRouteToNode(eq(nid))).thenReturn(prefix + nid.asString()));

            mockLoadMap.put(instanceId, load);
            mockInstanceConnectionMap.put(instanceId, mockInstanceConnection);

            return mockInstanceConnection;

        }).sorted((i0, i1) -> {
            final double load0 = i0.getInstanceMetadataContext().getInstanceLoad();
            final double load1 = i1.getInstanceMetadataContext().getInstanceLoad();
            return Double.compare(load0, load1);
        }).collect(toList()));

        when(getRemoteInvokerSupplier().get()).thenAnswer(i -> {

            final RemoteInvoker invoker = mock(RemoteInvoker.class);

            doAnswer(i0 -> {
                final String address = i0.getArgument(0);
                final String nodeIdString = address.substring(prefix.length());
                final NodeId nodeId = new NodeId(nodeIdString);
                mockRemoteInvokerMap.put(nodeId, invoker);
                latch.countDown();
                assertTrue(mockNodeIds.contains(nodeId), "Connected nodes does not contain: " + nodeId);
                return null;
            }).when(invoker).start(anyString());

            mockRemoteInvokerList.add(invoker);
            return invoker;

        });

        when(getInstanceConnectionService().getActiveConnections()).thenReturn(mockActiveConnections);

        // Start and Stop Service
        getRemoteInvokerRegistry().start();

        // Removes one using an event after the list has been built internally

        final InstanceConnection removed = mockActiveConnections.remove(0);
        getInstanceConnectionService().getOnDisconnectPublisher().publish(removed);

        // Determines the best instance based on the mocked load values.
        final InstanceId best = mockActiveConnections.get(0).getInstanceId();

        // Verify that the best instance is returned for each application when requesting any remote invoker.  Since
        // the remote invoker only knows based on connect address, we ensure that the remotes invoker was connected
        // to the correct address based on our own mock addressing scheme.
        mockApplicationUUIDs.forEach(a -> {
            final NodeId nodeId = new NodeId(best, a);
            final String addr = prefix + nodeId.asString();
            final PriorityRemoteInvoker priorityRemoteInvoker = (PriorityRemoteInvoker) getRemoteInvokerRegistry().getBestRemoteInvoker(a);
            verify(priorityRemoteInvoker.getDelegate(), times(1)).start(eq(addr));
        });

        // Verify that the requested remote invoker was connected to the correct node id
        mockInstanceIds.forEach(iid -> mockApplicationUUIDs.forEach(aid -> getRemoteInvokerRegistry().getAllRemoteInvokers(aid).forEach(ri -> {

            final NodeId nodeId = new NodeId(iid, aid);
            final RemoteInvoker mockRemoteInvoker = mockRemoteInvokerMap.get(nodeId);
            final PriorityRemoteInvoker priorityRemoteInvoker = (PriorityRemoteInvoker)ri;
            if (priorityRemoteInvoker.getDelegate() != mockRemoteInvoker) return;

            final String addr = prefix + nodeId.asString();
            verify(mockRemoteInvoker, times(1)).start(eq(addr));

        })));

        // Verifies that the requested remote invoker for the correct NodeId was actually connected tot he correct node id
        mockInstanceIds.forEach(iid -> mockApplicationUUIDs.forEach(aid -> {

            final NodeId nodeId = new NodeId(iid, aid);

            if (nodeId.equals(new NodeId(removed.getInstanceId(), aid))) {
                final RemoteInvoker remoteInvoker = mockRemoteInvokerMap.get(nodeId);
                assertNotNull(remoteInvoker);
                verify(remoteInvoker, times(1)).stop();
            } else {
                final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
                final String addr = prefix + nodeId.asString();
                verify(remoteInvoker, times(1)).start(eq(addr));
            }


        }));

        // Stops the service so we can finish verification
        getRemoteInvokerRegistry().stop();

        // Verify Mocks
        verify(getInstanceConnectionService(), atLeastOnce()).getActiveConnections();
        verify(getInstanceConnectionService(), times(1)).subscribeToConnect(any());
        verify(getInstanceConnectionService(), times(1)).subscribeToDisconnect(any());

        mockActiveConnections.forEach(c -> {

            final InstanceId instanceId = c.getInstanceId();

            verify(c, atLeastOnce()).getInstanceId();
            verify(c, atLeastOnce()).getInstanceMetadataContext();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getNodeIds();
            verify(c.getInstanceMetadataContext(), atLeastOnce()).getInstanceLoad();

            mockApplicationUUIDs.forEach(a -> {
                final NodeId nodeId = new NodeId(instanceId, a);
                verify(c, times(1)).openRouteToNode(eq(nodeId));
            });

        });

        mockRemoteInvokerList.forEach(ri -> verify(ri, times(1)).stop());

    }

    public Supplier<RemoteInvoker> getRemoteInvokerSupplier() {
        return remoteInvokerSupplier;
    }

    @Inject
    public void setRemoteInvokerSupplier(Supplier<RemoteInvoker> remoteInvokerSupplier) {
        this.remoteInvokerSupplier = remoteInvokerSupplier;
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

            final Supplier<RemoteInvoker> remoteInvokerSupplier = mock(Supplier.class);
            bind(new TypeLiteral<Supplier<RemoteInvoker>>(){}).toInstance(remoteInvokerSupplier);

            bind(RemoteInvoker.class).toProvider(remoteInvokerSupplier::get);
            bind(RemoteInvokerRegistry.class).to(SimpleRemoteInvokerRegistry.class).asEagerSingleton();

            bind(MockInstanceConnectionService.class).toInstance(spy(MockInstanceConnectionService.class));
            bind(InstanceConnectionService.class).to(MockInstanceConnectionService.class);

        }

    }

}
