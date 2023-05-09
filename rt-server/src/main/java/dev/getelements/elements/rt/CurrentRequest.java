package dev.getelements.elements.rt;

/**
 * Used to track the current {@link Request}.
 */
public class CurrentRequest {

    private static final ReentrantThreadLocal<Request> instance = new ReentrantThreadLocal<>();

    /**
     * Gets the shared instance.
     *
     * @return the {@link ReentrantThreadLocal<Attributes>} instance.
     */
    public static ReentrantThreadLocal<Request> getInstance() {
        return instance;
    }

}
