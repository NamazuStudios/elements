package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.remote.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;

/**
 * Aggregates all results.  Assumes that all results are derived from {@link List} and the final result is put
 * into a {@link List} combining all results.  When combining results this assumes that all {@link Consumer}s for the
 * asynchronous operations accept a {@link List} as well all method return values if not null/Void).
 */
public class ListAggregatePathRoutingStrategy extends AbstractAggregateRoutingStrategy {

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
        aggregate.addAll(lb);
        return aggregate;
    }

    @Override
    protected InvocationResult combine(final InvocationResult a, final InvocationResult b) {

        final List<Object> la = (List<Object>)a.getResult();
        final List<Object> lb = (List<Object>)b.getResult();

        final List<Object> aggregate = new ArrayList<>(la);
        aggregate.addAll(lb);

        final InvocationResult result = new InvocationResult();
        result.setResult(aggregate);

        return result;

    }

}
