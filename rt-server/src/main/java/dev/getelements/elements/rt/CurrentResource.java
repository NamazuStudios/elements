package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.util.ReentrantThreadLocal;

/**
 * Houses a {@link ReentrantThreadLocal} to represent the currently available
 * {@link ReentrantThreadLocal<Attributes>}
 */
public class CurrentResource {

    private static final ReentrantThreadLocal<Resource> instance = new ReentrantThreadLocal<>();

    /**
     * Gets the shared instance.
     *
     * @return the {@link ReentrantThreadLocal<Attributes>} instance.
     */
    public static ReentrantThreadLocal<Resource> getInstance() {
        return instance;
    }

}
