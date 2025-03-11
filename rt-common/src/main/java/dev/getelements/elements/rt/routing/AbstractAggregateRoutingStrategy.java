package dev.getelements.elements.rt.routing;

import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * A compex type of {@link RoutingStrategy} where each invocation goes to all known {@link RemoteInvoker} instances
 * and then combines the results together into a single result.  In the event of a single error, the whole call is
 * canceled rather than trying to partially report some result.
 *
 * It is not recommended that this {@link RoutingStrategy} be used for writes if reliability is expected, but only for
 * read operations.
 */
public abstract class AbstractAggregateRoutingStrategy implements RoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAggregateRoutingStrategy.class);

    private ApplicationId applicationId;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @Override
    public Future<Object> invokeFuture(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final List<RemoteInvoker> invokers = getRemoteInvokers(address);
        final int count = invokers.size();

        final List<Consumer<InvocationResult>> aggregateResultConsumerList = asyncInvocationResultConsumerList
            .stream()
            .map(c -> new AggregateConsumer<>(c, count, this::newInitialInvocationResult, this::combine))
            .collect(toList());

        final InvocationErrorConsumer firstInvocationErrorConsumer;
        firstInvocationErrorConsumer = new FirstInvocationErrorConsumer(asyncInvocationErrorConsumer);

        final List<CompletionStage<Object>> completionStages = invokers
            .stream()
            .map(ri -> ri.invokeCompletionStage(invocation, aggregateResultConsumerList, firstInvocationErrorConsumer))
            .collect(toList());

        return new AggregateFuture<>(completionStages, this::newInitialResult, this::combine);

    }

    @Override
    public AsyncOperation invokeAsync(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final var invokers = getRemoteInvokers(address);
        final int count = invokers.size();

        final List<Consumer<InvocationResult>> aggregateResultConsumerList = asyncInvocationResultConsumerList
            .stream()
            .map(c -> new AggregateConsumer<>(c, count, this::newInitialInvocationResult, this::combine))
            .collect(toList());

        final var firstInvocationErrorConsumer = new FirstInvocationErrorConsumer(asyncInvocationErrorConsumer);

        final var operations = invokers
            .stream()
            .map(ri -> ri.invokeAsync(invocation, aggregateResultConsumerList, firstInvocationErrorConsumer))
            .collect(Collectors.toList());

        return new AggregateAsyncOperation(operations);

    }

    @Override
    public Object invokeSync(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        final List<RemoteInvoker> invokers = getRemoteInvokers(address);
        final int count = invokers.size();

        final List<Consumer<InvocationResult>> aggregateResultConsumerList = asyncInvocationResultConsumerList
            .stream()
            .map(c -> new AggregateConsumer<>(c, count, this::newInitialInvocationResult, this::combine))
            .collect(toList());

        final InvocationErrorConsumer firstInvocationErrorConsumer;
        firstInvocationErrorConsumer = new FirstInvocationErrorConsumer(asyncInvocationErrorConsumer);

        Object combined = newInitialResult();

        for (final RemoteInvoker invoker : invokers) {

            final Object result = invoker.invokeSync(
                invocation,
                aggregateResultConsumerList,
                firstInvocationErrorConsumer);

            combined = combine(combined, result);

        }

        return combined;

    }

    /**
     * Gets the {@link List<RemoteInvoker>} for the supplied address.  By default this just gets all instances for a
     * particular application UUID.
     *
     * @param address the address
     * @return a {@link List<RemoteInvoker>} to use.
     */
    protected List<RemoteInvoker> getRemoteInvokers(final List<Object> address) {
        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);
        return getRemoteInvokerRegistry().getAllRemoteInvokers(getApplicationId());
    }

    /**
     * Gets the initial result to be fed into the {@link #combine(Object, Object)} call.  This should represent a
     * zero-state result.  For example, if combining {@link List} instances, this should ensure that it contains an
     * initial empty list.
     *
     * @return the {@link Object} for the initial aggregate operation.
     */
    protected abstract Object newInitialResult();

    /**
     * Creates a new {@link InvocationResult} which is used as the initial value for combining results.  This should be
     * a zero-state {@link InvocationResult}.  For example, if combining {@link List} instances, this should ensure that
     * it contains an initial empty list.
     *
     * @return the {@link InvocationResult} used as the initial value for aggregations
     */
    protected InvocationResult newInitialInvocationResult() {
        final InvocationResult invocationResult = new InvocationResult();
        invocationResult.setResult(newInitialResult());
        return invocationResult;
    }

    /**
     * Combines two raw {@link Object} results.
     *
     * @param a the first object
     * @param b the second object
     * @return the combined {@link Object}
     */
    protected abstract Object combine(Object a, Object b);

    /**
     * Combines two {@link InvocationResult} instances.  Specifically the returned {@link InvocationResult} shoud
     * contain the aggregate result in the {@link InvocationResult#getResult()} and return it.
     *
     * @param ra the first {@link InvocationResult}
     * @param rb the second {@link InvocationResult}
     * @return the {@link InvocationResult} combining the results of both a and b
     */
    protected InvocationResult combine(final InvocationResult ra, final InvocationResult rb) {
        final Object a = ra.getResult();
        final Object b = rb.getResult();
        ra.setResult(combine(a, b));
        return ra;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    @Inject
    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

}
