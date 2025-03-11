package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.Attributes;

import java.util.concurrent.TimeUnit;

public interface EventService {

    /**
     * Starts the {@link EventService} and makes it available to begin accepting tasks.
     */
    void start();

    /**
     * Stops the {@link EventService} and makes it unavailable to accept tasks.  Andy pending tasks are completed
     * with an exception and all references cleared.
     */
    void stop();

    /**
     * Registers a new {@link String} and set of consumers to handle the results.
     *
     * @param eventName the event name
     * @param attributes the {@link Attributes}.
     */
    void postAsync(String eventName, Attributes attributes,
                   long timeout, TimeUnit timeoutTimeUnit,
                   Object... args);

}
