package com.namazustudios.socialengine.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin<T> {

    private final List<T> list;

    private final AtomicInteger index = new AtomicInteger();

    public RoundRobin(final Collection<T> list) {

        if (list.isEmpty())
            throw new IllegalArgumentException("Must specify at least one entry in list.");

        this.list = List.copyOf(list);

    }

    public T next() {

        final var next = index.getAndUpdate(operand -> operand + 1 == list.size()
                ? 0
                : operand + 1
        );

        return list.get(next);

    }

}
