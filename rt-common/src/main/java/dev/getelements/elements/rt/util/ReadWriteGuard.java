package dev.getelements.elements.rt.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ReadWriteGuard {

    Lock getReadLock();

    Lock getWriteLock();

    Condition getCondition();

    default void ro(final Consumer<Condition> op) {
        computeRO(c -> {
            op.accept(c);
            return null;
        });
    }

    default void rw(final Consumer<Condition> op) {
        computeRW(c -> {
            op.accept(c);
            return null;
        });
    }

    <T> T computeRO(Function<Condition, T> op);

    <T> T computeRW(Function<Condition, T> op);

}
