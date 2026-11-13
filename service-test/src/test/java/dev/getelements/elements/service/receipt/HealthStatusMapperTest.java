package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.model.health.InstanceHealthStatus;
import dev.getelements.elements.sdk.model.health.RoutingHealthStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.rt.remote.InstanceStatus;
import dev.getelements.elements.rt.remote.RoutingStatus;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression test: DefaultHealthStatusService maps the rt ControlClient's RoutingStatus and
 * InstanceStatus into the health models through the shared MapperRegistry. The required mappings
 * were never registered, so the instance-connection health check failed with "No mapping exists
 * from RoutingStatus to RoutingHealthStatus" instead of reporting status.
 */
public class HealthStatusMapperTest {

    private MapperRegistry mapperRegistry;

    @BeforeMethod
    public void setUp() {

        final var provider = new ServicesMapperRegistryProvider();
        provider.setApiOutsideUrlProvider(() -> "http://localhost:8080/api/rest");

        mapperRegistry = provider.get();

    }

    @Test
    public void routingStatusMapsToRoutingHealthStatus() {

        final var routingStatus = fakeRoutingStatus();

        final var routingHealthStatus = mapperRegistry.map(routingStatus, RoutingHealthStatus.class);

        assertNotNull(routingHealthStatus);
        assertEquals(routingHealthStatus.getInstanceId(), "instance-1");
        assertEquals(routingHealthStatus.getRoutingTable(), List.of("jeromq://host-1:7001"));
        assertEquals(routingHealthStatus.getMasterNodeRoutingTable(), List.of("jeromq://master:7000"));
        assertEquals(routingHealthStatus.getApplicationNodeRoutingTable(), List.of("jeromq://host-1:7001"));

    }

    @Test
    public void instanceStatusMapsToInstanceHealthStatus() {

        final var instanceStatus = fakeInstanceStatus();

        final var instanceHealthStatus = mapperRegistry.map(instanceStatus, InstanceHealthStatus.class);

        assertNotNull(instanceHealthStatus);
        assertEquals(instanceHealthStatus.getInstanceId(), "instance-1");
        assertEquals(instanceHealthStatus.getNodeIds(), List.of("node-1", "node-2"));

    }

    private RoutingStatus fakeRoutingStatus() {

        return new RoutingStatus() {

            @Override
            public InstanceId getInstanceId() {
                return fakeInstanceId();
            }

            @Override
            public List<Route> getRoutingTable() {
                return List.of(fakeRoute("jeromq://host-1:7001"));
            }

            @Override
            public List<Route> getMasterNodeRoutingTable() {
                return List.of(fakeRoute("jeromq://master:7000"));
            }

            @Override
            public List<Route> getApplicationNodeRoutingTable() {
                return List.of(fakeRoute("jeromq://host-1:7001"));
            }

        };

    }

    private InstanceStatus fakeInstanceStatus() {

        return new InstanceStatus() {

            @Override
            public InstanceId getInstanceId() {
                return fakeInstanceId();
            }

            @Override
            public List<NodeId> getNodeIds() {
                return List.of(fakeNodeId("node-1"), fakeNodeId("node-2"));
            }

        };

    }

    private RoutingStatus.Route fakeRoute(final String physicalDestination) {

        return new RoutingStatus.Route() {

            @Override
            public boolean isLocal() {
                return false;
            }

            @Override
            public NodeId getNodeId() {
                return fakeNodeId("node-1");
            }

            @Override
            public String getPhysicalDestination() {
                return physicalDestination;
            }

        };

    }

    private InstanceId fakeInstanceId() {
        final var instanceId = org.mockito.Mockito.mock(InstanceId.class);
        org.mockito.Mockito.when(instanceId.toString()).thenReturn("instance-1");
        return instanceId;
    }

    private NodeId fakeNodeId(final String id) {
        final var nodeId = org.mockito.Mockito.mock(NodeId.class);
        org.mockito.Mockito.when(nodeId.toString()).thenReturn(id);
        return nodeId;
    }

}
