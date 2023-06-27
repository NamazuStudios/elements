package dev.getelements.elements.rt;

/**
 * Houses a {@link ReentrantThreadLocal<Attributes>} to represent the currently available
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
