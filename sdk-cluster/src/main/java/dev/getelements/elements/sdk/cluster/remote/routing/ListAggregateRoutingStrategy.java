package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult;
import dev.getelements.elements.sdk.cluster.remote.RemoteInvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static dev.getelements.elements.sdk.cluster.remote.routing.RoutingUtility.reduceAddressToNodeIds;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Aggregates all results.  Assumes that all results are derived from {@link List} and the final result is put
 * into a {@link List} combining all results.  When combining results this assumes that all {@link Consumer}s for the
 * asynchronous operations accept a {@link List} as well all method return values if not null/Void).
 */
public class ListAggregateRoutingStrategy extends AbstractAggregateRoutingStrategy {

    @Override
    protected Object newInitialResult() {
        return new ArrayList<>();
    }

    @Override
    protected InvocationResult newInitialInvocationResult() {
        return new InvocationResult(emptyList());
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

        final List<Object> la = (List<Object>)a.result();
        final List<Object> lb = (List<Object>)b.result();

        final List<Object> aggregate = new ArrayList<>(la);
        aggregate.addAll(lb == null ? emptyList() : lb);

        return new InvocationResult(aggregate);

    }

}
