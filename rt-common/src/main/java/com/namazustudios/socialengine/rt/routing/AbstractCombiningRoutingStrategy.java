package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * A compex type of {@link RoutingStrategy} where each invocation goes to all known {@link RemoteInvoker} instances
 * and then combines the results together into a single result.  In the event of a single error, the whole call is
 * canceled rather than trying to partially report some result.
 *
 * It is not recommended that this {@link RoutingStrategy} be used for writes if reliability is expected, but only for
 * read operations.
 */
public abstract class AbstractCombiningRoutingStrategy implements RoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCombiningRoutingStrategy.class);

    private UUID defaultApplicationId;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @Override
    public Future<Object> invokeFuture(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);

        final InvocationResult initial = newInitialInvocationResult();

        final List<RemoteInvoker> invokers = getRemoteInvokerRegistry().getAllRemoteInvokers(getDefaultApplicationId());
        final int count = invokers.size();

        final List<Consumer<InvocationResult>> aggregateResultConsumerList = asyncInvocationResultConsumerList
            .stream()
            .map(c -> new AggregateConsumer<>(c, count, initial, this::combine))
            .collect(toList());

        final InvocationErrorConsumer firstInvocationErrorConsumer;
        firstInvocationErrorConsumer = new FirstInvocationErrorConsumer(asyncInvocationErrorConsumer);

        final List<CompletionStage<Object>> completionStages = invokers
            .stream()
            .map(ri -> ri.invokeCompletionStage(invocation, aggregateResultConsumerList, firstInvocationErrorConsumer))
            .collect(toList());

        return new AggregateFuture<>(completionStages, emptyList(), this::combine);

    }

    @Override
    public Void invokeAsync(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);

        final InvocationResult initial = newInitialInvocationResult();

        final List<RemoteInvoker> invokers = getRemoteInvokerRegistry().getAllRemoteInvokers(getDefaultApplicationId());
        final int count = invokers.size();

        final List<Consumer<InvocationResult>> aggregateResultConsumerList = asyncInvocationResultConsumerList
            .stream()
            .map(c -> new AggregateConsumer<>(c, count, initial, this::combine))
            .collect(toList());

        final InvocationErrorConsumer firstInvocationErrorConsumer;
        firstInvocationErrorConsumer = new FirstInvocationErrorConsumer(asyncInvocationErrorConsumer);

        invokers
            .stream()
            .map(ri -> ri.invokeAsync(invocation, aggregateResultConsumerList, firstInvocationErrorConsumer))
            .collect(toList());

        return null;

    }

    @Override
    public Object invokeSync(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);

        final InvocationResult initial = newInitialInvocationResult();

        final List<RemoteInvoker> invokers = getRemoteInvokerRegistry().getAllRemoteInvokers(defaultApplicationId);
        final int count = invokers.size();

        final List<Consumer<InvocationResult>> aggregateResultConsumerList = asyncInvocationResultConsumerList
            .stream()
            .map(c -> new AggregateConsumer<>(c, count, initial, this::combine))
            .collect(toList());

        final InvocationErrorConsumer firstInvocationErrorConsumer;
        firstInvocationErrorConsumer = new FirstInvocationErrorConsumer(asyncInvocationErrorConsumer);

        final List<Object> combined = new ArrayList<>();

        for (final RemoteInvoker invoker : invokers) {

            final List<Object> o = (List<Object>) invoker.invokeSync(
                invocation,
                aggregateResultConsumerList,
                firstInvocationErrorConsumer);

            combined.addAll(o);

        }

        return combined;

    }

    /**
     * Creates a new {@link InvocationResult} which is used as the initial value for combining results.  This should be
     * a zero-state {@link InvocationResult}.  For example, if combining {@link List} instances, this should ensure that
     * it contains an initial empty list.
     *
     * @return the {@link InvocationResult} used as the initial value for aggregations
     */
    protected abstract InvocationResult newInitialInvocationResult();

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
     * @param a the first {@link InvocationResult}
     * @param b the second {@link InvocationResult}
     * @return the {@link InvocationResult} combining the results of both a and b
     */
    protected abstract InvocationResult combine(InvocationResult a, InvocationResult b);

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

    public UUID getDefaultApplicationId() {
        return defaultApplicationId;
    }

    @Inject
    public void setDefaultApplicationId(@Named(DEFAULT_APPLICATION) UUID defaultApplicationId) {
        this.defaultApplicationId = defaultApplicationId;
    }

}
