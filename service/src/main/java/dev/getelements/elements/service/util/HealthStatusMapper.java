package dev.getelements.elements.service.util;

import dev.getelements.elements.rt.remote.InstanceStatus;
import dev.getelements.elements.rt.remote.RoutingStatus;
import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.model.health.InstanceHealthStatus;
import dev.getelements.elements.sdk.model.health.RoutingHealthStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps the runtime status reported by the rt {@code ControlClient} into the health models exposed
 * by {@code DefaultHealthStatusService}.
 *
 * <p>The rt types are converted to their string forms: routes render as their physical
 * destination (the same string the routing-table health checks match known hosts against), and
 * instance/node ids render via their standard string form.
 */
@Mapper
@ElementPrivate
public interface HealthStatusMapper {

    @Mapping(target = "routingTable", source = "routingTable")
    RoutingHealthStatus toRoutingHealthStatus(RoutingStatus routingStatus);

    InstanceHealthStatus toInstanceHealthStatus(InstanceStatus instanceStatus);

    default String routeToString(final RoutingStatus.Route route) {
        return route.getPhysicalDestination();
    }

    default String instanceIdToString(final InstanceId instanceId) {
        return instanceId.toString();
    }

    default String nodeIdToString(final NodeId nodeId) {
        return nodeId.toString();
    }

}
