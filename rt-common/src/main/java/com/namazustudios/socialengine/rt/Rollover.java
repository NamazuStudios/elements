package com.namazustudios.socialengine.rt;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class Rollover {

    private final AtomicInteger value = new AtomicInteger();

    private final AtomicInteger upper = new AtomicInteger();

    private final IntUnaryOperator operator = operand -> (operand + 1) == upper.get() ? 0 : operand + 1;

    public Rollover() {
        this(Integer.MAX_VALUE);
    }

    public Rollover(final int upper) {
        if (upper < 1) throw new IllegalArgumentException("upper limit must be > 0");
        this.upper.set(upper);
    }

    public int getAndIncrement() {
        final int value = this.value.getAndUpdate(operator);
        return value;
    }

}
