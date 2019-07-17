package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Consumer;

class PriorityRemoteInvoker implements RemoteInvoker, Comparable<PriorityRemoteInvoker> {

    private final double priority;

    private final RemoteInvoker delegate;

    public PriorityRemoteInvoker(final RemoteInvoker delegate, final double priority) {
        this.priority = priority;
        this.delegate = delegate;
    }

    @Override
    public void start(String connectAddress) {
        delegate.start(connectAddress);
    }

    @Override
    public void start(String connectAddress, int timeoutMillis) {
        delegate.start(connectAddress, timeoutMillis);
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    @Deprecated
    public Future<Object> invoke(Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invoke(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public Void invokeAsync(Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invokeAsync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public Future<Object> invokeFuture(Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invokeFuture(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public CompletionStage<Object> invokeCompletionStage(Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invokeCompletionStage(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public Object invokeSync(Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {
        return delegate.invokeSync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public int compareTo(PriorityRemoteInvoker other) {
        return Double.compare(priority, other.priority);
    }

    public RemoteInvoker getDelegate() {
        return delegate;
    }

}
