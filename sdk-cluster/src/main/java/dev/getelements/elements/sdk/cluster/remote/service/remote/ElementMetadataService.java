package dev.getelements.elements.sdk.cluster.remote.service.remote;

import dev.getelements.elements.sdk.cluster.remote.RemoteElement;
import dev.getelements.elements.sdk.cluster.remote.annotation.Proxyable;
import dev.getelements.elements.sdk.cluster.remote.annotation.RemotelyInvokable;
import dev.getelements.elements.sdk.cluster.remote.annotation.Routing;
import dev.getelements.elements.sdk.cluster.remote.record.RemoteElementRecord;
import dev.getelements.elements.sdk.cluster.remote.routing.ListAggregateRoutingStrategy;

import java.util.List;

/**
 * A remote type made available which can be used to query the instance and report available remote elements.
 */
@Proxyable
public interface ElementMetadataService {

    /**
     * Reports all available remote elements. This uses the cluster fabric to get a snapshot of all instances and what
     * {@link RemoteElement}s they support.
     *
     * @return all available remote elements.
     */
    @RemotelyInvokable(routing = @Routing(ListAggregateRoutingStrategy.class))
    List<RemoteElementRecord> getAvailableElements();

}
