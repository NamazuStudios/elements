package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.cluster.remote.AsyncOperation;

/**
 * An {@link AsyncOperation} which fans {@link #cancel()} out to a set of underlying {@link AsyncOperation}s, used to
 * control a group of remote invocations issued by an aggregate {@link RoutingStrategy} (such as
 * {@link AbstractAggregateRoutingStrategy}) as a single unit.
 */
public class AggregateAsyncOperation implements AsyncOperation {

    private final Iterable<AsyncOperation> operations;

    public AggregateAsyncOperation(Iterable<AsyncOperation> operations) {
        this.operations = operations;
    }

    @Override
    public void cancel() {
        for (var op : operations) op.cancel();
    }

}
