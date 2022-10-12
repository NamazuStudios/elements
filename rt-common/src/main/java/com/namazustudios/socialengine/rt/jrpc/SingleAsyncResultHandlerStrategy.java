package com.namazustudios.socialengine.rt.jrpc;

import com.namazustudios.socialengine.rt.ConcurrentLockedPublisher;
import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.ResultHandlerStrategy;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.remote.InvocationError;
import com.namazustudios.socialengine.rt.remote.InvocationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Represents a single async style RPC method. This supports a single return type, either asynchronous or synchronous.
 * This is useful for JSON-RPC where the response document tends to be a single object, rather than a series of
 * responses sent during the invocation process.
 */
public class SingleAsyncResultHandlerStrategy implements ResultHandlerStrategy {

    private static final Logger logger = LoggerFactory.getLogger(SingleSyncReturnResultHandlerStrategy.class);

    private final AtomicBoolean finished = new AtomicBoolean();

    private final Lock lock = new ReentrantLock();

    private final Publisher<Throwable> onError = new ConcurrentLockedPublisher<>(lock);

    private final Publisher<Object> onResult = new ConcurrentLockedPublisher<>(lock);

    private final Consumer<InvocationError> invocationErrorConsumer = ie -> {
        if (finished.compareAndSet(false, true)) {
            onError.publish(ie.getThrowable());
        } else {
            logger.warn("Got unexpected error.", ie.getThrowable());
        }
    };

    private final Consumer<InvocationResult> invocationResultConsumer = ir -> {
        if (finished.compareAndSet(false, true)) {
            onResult.publish(ir.getResult());
        } else {
            logger.warn("Got unexpected error: {}", ir.getResult());
        }
    };

    @Override
    public Consumer<InvocationResult> getSyncResultConsumer() {
        return ir -> logger.error("Got sync error when not expected: {}", ir.getResult());
    }

    @Override
    public Consumer<InvocationError> getSyncErrorConsumer() {
        return ie -> logger.error("Got sync error when not expected.", ie.getThrowable());
    }

    @Override
    public List<Consumer<InvocationResult>> getAsyncInvocationResultConsumers() {
        return List.of(invocationResultConsumer);
    }

    @Override
    public Consumer<InvocationError> getAsyncInvocationErrorConsumer() {
        return invocationErrorConsumer;
    }

    @Override
    public Subscription onError(final Consumer<Throwable> finalResult) {
        return onError.subscribe(finalResult);
    }

    @Override
    public Subscription onFinalResult(final Consumer<Object> finalResult) {
        return onResult.subscribe(finalResult);
    }

}
