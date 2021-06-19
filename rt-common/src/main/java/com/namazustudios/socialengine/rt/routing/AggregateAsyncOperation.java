package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.remote.AsyncOperation;

import java.util.concurrent.TimeUnit;

public class AggregateAsyncOperation implements AsyncOperation {

    private final Iterable<AsyncOperation> operations;

    public AggregateAsyncOperation(Iterable<AsyncOperation> operations) {
        this.operations = operations;
    }

    @Override
    public void cancel() {
        for (var op : operations) op.cancel();
    }

    @Override
    public void timeout(long time, TimeUnit timeUnit) {
        for (var op : operations) op.timeout(time, timeUnit);
    }

}
