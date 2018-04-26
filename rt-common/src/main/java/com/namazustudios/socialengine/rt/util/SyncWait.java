package com.namazustudios.socialengine.rt.util;

import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a one-way pipe which can be used to relay either an {@link Exception} or a successful result.  This will
 * only accept the first of an exception or a result whichever comes first.
 */
public class SyncWait<ResultT> {

    private final Logger logger;

    private final AtomicReference<Supplier<ResultT>> supplierAtomicReference = new AtomicReference<>();

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final Consumer<ResultT> resultTConsumer = r -> {
        if (supplierAtomicReference.compareAndSet(null, () -> r)) {
            countDownLatch.countDown();
        }
    };

    private final Consumer<Throwable> throwableConsumer = th -> {
        if (supplierAtomicReference.compareAndSet(null, () -> {
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            } else {
                throw new InternalException(th);
            }
        })) countDownLatch.countDown();
    };

    /**
     * Constructs an instance of {@link SyncWait} with the given {@link Class} from which to create a {@link Logger}
     * instance
     *
     * @param cls the {@link Class<?>}
     */
    public SyncWait(final Class<?> cls) {
        this(LoggerFactory.getLogger(cls));
    }

    /**
     * Constructs an instance of {@link SyncWait} with the given {@link Logger} for logging errors.
     *
     * @param logger the {@link Logger}
     */
    public SyncWait(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns the result, or throws a {@link Throwable}.
     *
     * @return the result
     */
    public ResultT get() {
        try {
            countDownLatch.await();
            return supplierAtomicReference.get().get();
        } catch (InterruptedException e) {
            logger.info("Interrupted waiting for result.", e);
            throw new InternalException(e);
        }
    }

    /**
     * Returns a {@link Consumer<ResultT>} for accepting the result.
     *
     * @return the {@link Consumer<ResultT>}
     */
    public Consumer<ResultT> getResultConsumer() {
        return resultTConsumer;
    }

    /**
     * Returns a {@link Consumer<Throwable>} for accepting the error.
     *
     * @return the {@link Consumer<Throwable>}
     */
    public Consumer<Throwable> getErrorConsumer() {
        return throwableConsumer;
    }

}
