package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;

/**
 * Inspects the attributes, parameters, and annotations of a specific {@link Method} to build an instance of
 * {@link InvocationDispatcher} to dispatch {@link Invocation} instances to a local object in memory.
 */
public class LocalInvocationDispatcherBuilder {

    private final Method method;

    public LocalInvocationDispatcherBuilder(Method method) {
        this.method = method;
    }

    /**
     * Builds a new instance of the {@link InvocationDispatcher}.
     *
     * @return returns the {@link InvocationDispatcher}
     */
    public InvocationDispatcher build() {
        return null;
    }

}
