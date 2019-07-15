package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.remote.InvocationError;
import com.namazustudios.socialengine.rt.remote.InvocationErrorConsumer;

import java.util.concurrent.atomic.AtomicBoolean;

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
