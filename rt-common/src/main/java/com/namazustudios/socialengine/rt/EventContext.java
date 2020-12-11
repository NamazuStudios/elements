package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;

/**
 *
 */
@Proxyable
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
    void postAsync(@Serialize String eventName,
                   @Serialize Attributes attributes,
                   @Serialize Object... args);
}
