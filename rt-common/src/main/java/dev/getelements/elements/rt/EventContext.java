package dev.getelements.elements.rt;

import dev.getelements.elements.rt.annotation.*;

import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.WORKER_SCOPE;

/**
 * Manages events.
 */
@Proxyable
@RemoteService(scopes = @RemoteScope(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
public interface EventContext {

    /**
     * The timeout for the {@link EventContext} in milliseconds.
     */
    String EVENT_TIMEOUT_MSEC = "dev.getelements.elements.rt.handler.timeout.event";

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
