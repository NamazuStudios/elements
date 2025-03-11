package dev.getelements.elements.rt;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ConcurrentRoundRobin<T> implements RoundRobin<T> {

    private final T[] objects;

    private final Rollover next;

    public ConcurrentRoundRobin(final T[] objects, int size) {
        this.next = new Rollover(size);
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
        return objects[next];
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < objects.length;
            }

            @Override
            public T next() {
                return objects[i++];
            }

        };
    }

    @Override
    public Stream<T> stream() {
        return Arrays.stream(objects);
    }

}
