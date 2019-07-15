package com.namazustudios.socialengine.rt.routing;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

/**
 * Given the expected number of calls, this generates a {@link Consumer<T> which will aggregate all results together
 * until a set number of responses occur.  Once the last invocation occurs, this instance will invoke the delegate
 * {@link Consumer} instance to finis the call.
 *
 * @param <T>
 */
public class AggregateConsumer<T> implements Consumer<T> {


    private final AtomicInteger count;

    private final AtomicReference<T> aggregate;

    private final Consumer<T> delegate;

    private final BinaryOperator<T> aggregator;

    public AggregateConsumer(final Consumer<T> delegate,
                             final int expected,
                             final T inital,
                             final BinaryOperator<T> aggregator) {
        this.count = new AtomicInteger(expected);
        this.delegate = delegate;
        this.aggregator = aggregator;
        this.aggregate = new AtomicReference<>(inital);
    }

    @Override
    public void accept(final T t) {

        final T result = aggregate.accumulateAndGet(t, aggregator);

        if (count.decrementAndGet() == 0) {
            delegate.accept(result);
        }

    }

    /**
     * Returns a {@link Consumer<U>} that will cancel the call to the delegate {@link Consumer} specified in this
     * instance.
     *
     * @param delegate the the delegate {@link Consumer<U>} that will be called if the returned {@link Consumer<U>} is
     *                 called.
     * @param <U>
     */
    public <U> Consumer<U> newCancelConsumer(final Consumer<U> delegate) {
        return u -> {
            if (count.getAndSet(0) > 0) delegate.accept(u);
        };
    }

}
