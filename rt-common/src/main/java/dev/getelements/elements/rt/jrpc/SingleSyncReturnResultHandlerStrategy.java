package dev.getelements.elements.rt.jrpc;

import dev.getelements.elements.sdk.util.ConcurrentLockedPublisher;
import dev.getelements.elements.sdk.util.Publisher;
import dev.getelements.elements.rt.ResultHandlerStrategy;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.rt.remote.InvocationError;
import dev.getelements.elements.rt.remote.InvocationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;

/**
 * Represents a single return style RPC method. This supports a single return type, either asynchronous or synchronous.
 * This is useful for JSON-RPC where the response document tends to be a single object, rather than a series of
 * responses sent during the invocation process.
 */
public class SingleSyncReturnResultHandlerStrategy implements ResultHandlerStrategy {

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
        return invocationResultConsumer;
    }

    @Override
    public Consumer<InvocationError> getSyncErrorConsumer() {
        return invocationErrorConsumer;
    }

    @Override
    public List<Consumer<InvocationResult>> getAsyncInvocationResultConsumers() {
        return emptyList();
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
