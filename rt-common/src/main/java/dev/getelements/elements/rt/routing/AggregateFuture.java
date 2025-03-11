package dev.getelements.elements.rt.routing;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.ConcurrentHashMap.newKeySet;

public class AggregateFuture<AggregateT> implements Future<AggregateT> {

    private final CompletableFuture<AggregateT> delegate;

    public AggregateFuture(final Iterable<CompletionStage<AggregateT>> completionStages,
                           final Supplier<AggregateT> initial,
                           final BinaryOperator<AggregateT> aggregator) {

        final CompletableFuture<AggregateT> first = new CompletableFuture<>();
        final Iterator<CompletionStage<AggregateT>> itr = completionStages.iterator();

        CompletionStage<AggregateT> prev = first;

        while (itr.hasNext()) {
           final CompletionStage<AggregateT> next = itr.next();
           prev = prev.thenCombineAsync(next, aggregator);
        }

        delegate = prev.toCompletableFuture();
        first.complete(initial.get());

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public AggregateT get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public AggregateT get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }

}
