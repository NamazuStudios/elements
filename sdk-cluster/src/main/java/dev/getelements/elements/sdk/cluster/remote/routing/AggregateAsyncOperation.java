package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.cluster.remote.AsyncOperation;

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
