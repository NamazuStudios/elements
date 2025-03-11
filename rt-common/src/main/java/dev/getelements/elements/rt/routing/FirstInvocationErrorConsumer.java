package dev.getelements.elements.rt.routing;

import dev.getelements.elements.rt.remote.InvocationError;
import dev.getelements.elements.rt.remote.InvocationErrorConsumer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A type of {@link InvocationErrorConsumer} which accepts an instance of {@link Throwable} and forwards it to the
 * delegate.  Only the first error is relayed, and all others simply ignored as only one call needs to fail for
 * aggregation to fail.
 */
public class FirstInvocationErrorConsumer implements InvocationErrorConsumer {

    private final InvocationErrorConsumer delegate;

    private final AtomicBoolean done = new AtomicBoolean();

    public FirstInvocationErrorConsumer(final InvocationErrorConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void accept(InvocationError e) {
        if (done.compareAndSet(false, true)) delegate.accept(e);
    }

}
