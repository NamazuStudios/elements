package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class PriorityRemoteInvoker implements RemoteInvoker, Comparable<PriorityRemoteInvoker> {

    private final NodeId nodeId;

    private final double priority;

    private final RemoteInvoker delegate;

    public PriorityRemoteInvoker(final RemoteInvoker delegate, final NodeId nodeId, final double priority) {
        this.nodeId = nodeId;
        this.priority = priority;
        this.delegate = delegate;
    }

    @Override
    public void start(final String connectAddress) {
        delegate.start(connectAddress);
    }

    @Override
    public void start(final String connectAddress,
                      final long timeout,
                      final TimeUnit timeoutTimeUnit) {
        delegate.start(connectAddress, timeout, timeoutTimeUnit);
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    @Deprecated
    public Future<Object> invoke(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invoke(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public Void invokeAsyncV(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invokeAsyncV(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public AsyncOperation invokeAsync(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invokeAsync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public Future<Object> invokeFuture(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invokeFuture(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public CompletionStage<Object> invokeCompletionStage(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return delegate.invokeCompletionStage(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public Object invokeSync(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {
        return delegate.invokeSync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public int compareTo(final PriorityRemoteInvoker other) {
        return Double.compare(priority, other.priority);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PriorityRemoteInvoker{");
        sb.append("priority=").append(priority);
        sb.append(", delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    public double getPriority() {
        return priority;
    }

    public RemoteInvoker getDelegate() {
        return delegate;
    }

    public boolean isSameDelegate(final PriorityRemoteInvoker other) {
        return delegate == other.delegate;
    }

    public PriorityRemoteInvoker reprioritize(final double quality) {
        return new PriorityRemoteInvoker(delegate, nodeId, quality);
    }

}
