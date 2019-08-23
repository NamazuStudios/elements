package com.namazustudios.socialengine.rt;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentRoundRobin<T> implements RoundRobin<T> {

    private final T[] objects;

    private final AtomicInteger next = new AtomicInteger();

    private ConcurrentRoundRobin(final T[] objects, int size) {
        this.objects = Arrays.copyOf(objects, size);
    }

    @Override
    public T set(int index, T object) {
        return objects[index] = object;
    }

    @Override
    public T get(int index) {
        return objects[index];
    }

    @Override
    public T getNext() {
        final int next = this.next.getAndIncrement();
        return objects[next % objects.length];
    }

}
