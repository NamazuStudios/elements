package com.namazustudios.socialengine.rt;

/**
 * Houses a {@link ReentrantThreadLocal<Attributes>} to represent the currently available
 * {@link ReentrantThreadLocal<Attributes>}
 */
public class CurrentAttributes {

    private static final ReentrantThreadLocal<Attributes> instance = new ReentrantThreadLocal<>();

    /**
     * Gets the shared instance.
     *
     * @return the {@link ReentrantThreadLocal<Attributes>} instance.
     */
    public static ReentrantThreadLocal<Attributes> getInstance() {
        return instance;
    }

}
