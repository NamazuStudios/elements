package com.namazustudios.socialengine.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Allows for round-robin iteration of a list of objects in a thread safe.
 *
 * @param <T>
 */
public class RoundRobin<T> implements Iterable<T> {

    private final List<T> list;

    private final AtomicInteger index = new AtomicInteger();

    /**
     * Constructs a {@link RoundRobin<T>} from the supplied {@link Collection<T>}
     *
     * @param stream the {@link Stream<T>} backing this {@link RoundRobin<T>}
     */
    public RoundRobin(final Stream<T> stream) {
        this.list = stream.collect(toList());
    }

    /**
     * Constructs a {@link RoundRobin<T>} from the supplied {@link Collection<T>}
     *
     * @param collection the {@link Collection<T>} backing this {@link RoundRobin<T>}
     */
    public RoundRobin(final Collection<T> collection) {

        if (collection.isEmpty())
            throw new IllegalArgumentException("Must specify at least one entry in list.");

        this.list = List.copyOf(collection);

    }

    /**
     * Gets the next item in the sequence.
     *
     * @return the next item
     */
    public T next() {

        final var next = index.getAndUpdate(operand -> operand + 1 == list.size()
                ? 0
                : operand + 1
        );

        return list.get(next);

    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return RoundRobin.this.next();
            }
        };
    }

}
