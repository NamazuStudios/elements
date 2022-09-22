package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;

import static com.namazustudios.socialengine.rt.annotation.RemoteServiceDefinition.ELEMENTS_RT_PROTOCOL;
import static com.namazustudios.socialengine.rt.annotation.RemoteServiceDefinition.WORKER_SCOPE;

/**
 * Manages events.
 */
@Proxyable
@RemoteService(@RemoteServiceDefinition(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
public interface EventContext {

    /**
     * The timeout for the {@link EventContext} in milliseconds.
     */
    String EVENT_TIMEOUT_MSEC = "com.namazustudios.socialengine.rt.handler.timeout.event";

    /**
     * Starts this {@link EventContext}.
     */
    default void start() {}

    /**
     * Stops this {@link EventContext}.
     */
    default void stop() {}

    @RemotelyInvokable
    @Dispatch(Dispatch.Type.ASYNCHRONOUS)
    void postAsync(@Serialize String eventName,
                   @Serialize Attributes attributes,
                   @Serialize Object... args);

}
