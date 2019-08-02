package com.namazustudios.socialengine.rt.routing;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
                             final Supplier<T> initalSupplier,
                             final BinaryOperator<T> aggregator) {
        this.count = new AtomicInteger(expected);
        this.delegate = delegate;
        this.aggregator = aggregator;
        this.aggregate = new AtomicReference<>(initalSupplier.get());
    }

    @Override
    public void accept(final T t) {

        final T result = aggregate.accumulateAndGet(t, aggregator);

        if (count.decrementAndGet() == 0) {
            delegate.accept(result);
        }

    }

}
