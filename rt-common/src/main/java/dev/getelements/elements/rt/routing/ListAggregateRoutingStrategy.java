package dev.getelements.elements.rt.routing;

import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.InvocationResult;
import dev.getelements.elements.rt.remote.RemoteInvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.routing.RoutingUtility.reduceAddressToNodeIds;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Aggregates all results.  Assumes that all results are derived from {@link List} and the final result is put
 * into a {@link List} combining all results.  When combining results this assumes that all {@link Consumer}s for the
 * asynchronous operations accept a {@link List} as well all method return values if not null/Void).
 */
public class ListAggregateRoutingStrategy extends AbstractAggregateRoutingStrategy {

    @Override
    protected List<RemoteInvoker> getRemoteInvokers(List<Object> address) {

        // If no address is specified, fall back to the default behavior, which is fan out to all nodes
        if (address.isEmpty()) return super.getRemoteInvokers(address);

        final Set<NodeId> nodeIdSet = reduceAddressToNodeIds(address);

        // Ensures that if anywhere a NodeId is left blank (wildcard) it will route to all remote invokers
        if (nodeIdSet.contains(null)) return getRemoteInvokerRegistry().getAllRemoteInvokers(getApplicationId());

        // Collects the NodeIds to a list of RemoteInvoker
        return nodeIdSet
            .stream()
            .map(nid -> getRemoteInvokerRegistry().getRemoteInvoker(nid))
            .collect(toList());

    }

    @Override
    protected Object newInitialResult() {
        return new ArrayList<>();
    }

    @Override
    protected InvocationResult newInitialInvocationResult() {
        final InvocationResult invocationResult = new InvocationResult();
        invocationResult.setResult(emptyList());
        return invocationResult;
    }

    @Override
    protected Object combine(final Object a, final Object b) {
        final List<Object> la = (List<Object>)a;
        final List<Object> lb = (List<Object>)b;
        final List<Object> aggregate = new ArrayList<>(la);
        aggregate.addAll(lb == null ? emptyList() : lb);
        return aggregate;
    }

    @Override
    protected InvocationResult combine(final InvocationResult a, final InvocationResult b) {

        final List<Object> la = (List<Object>)a.getResult();
        final List<Object> lb = (List<Object>)b.getResult();

        final List<Object> aggregate = new ArrayList<>(la);
        aggregate.addAll(lb == null ? emptyList() : lb);

        final InvocationResult result = new InvocationResult();
        result.setResult(aggregate);

        return result;

    }

}
