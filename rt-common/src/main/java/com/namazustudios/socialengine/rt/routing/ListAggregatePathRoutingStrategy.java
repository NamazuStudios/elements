package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static java.util.stream.Collectors.toList;

/**
 * Aggregates all results.  Assumes that all results are derived from {@link Collection} and the final result is put
 * into a {@link Collection} of some type which is compatible with the return type of the method.
 */
public class ListAggregatePathRoutingStrategy implements RoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ListAggregatePathRoutingStrategy.class);

    @Override
    public Future<Object> invokeFuture(
            final List<Object> address,
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        if (!address.isEmpty()) logger.warn("Ignorning routing address {}", address);

        final InvocationResult initial = new InvocationResult();
        initial.setResult(emptyList());

        final List<RemoteInvoker> invokers = remoteInvokerRegistry.getAllRemoteInvokers();
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
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        if (!address.isEmpty()) logger.warn("Ignorning routing address {}", address);

        final InvocationResult initial = new InvocationResult();
        initial.setResult(emptyList());

        final List<RemoteInvoker> invokers = remoteInvokerRegistry.getAllRemoteInvokers();
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
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        if (!address.isEmpty()) logger.warn("Ignorning routing address {}", address);

        final InvocationResult initial = new InvocationResult();
        initial.setResult(emptyList());

        final List<RemoteInvoker> invokers = remoteInvokerRegistry.getAllRemoteInvokers();
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

    private Object combine(final Object a, final Object b) {
        final List<Object> la = (List<Object>)a;
        final List<Object> lb = (List<Object>)b;
        final List<Object> aggregate = new ArrayList<>(la);
        aggregate.addAll(lb);
        return aggregate;
    }


    private InvocationResult combine(final InvocationResult a, final InvocationResult b) {

        final List<Object> la = (List<Object>)a.getResult();
        final List<Object> lb = (List<Object>)b.getResult();

        final List<Object> aggregate = new ArrayList<>(la);
        aggregate.addAll(lb);

        final InvocationResult result = new InvocationResult();
        result.setResult(aggregate);

        return result;

    }

}
