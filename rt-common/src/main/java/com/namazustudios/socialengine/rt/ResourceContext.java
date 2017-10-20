package com.namazustudios.socialengine.rt;

/**
 * The interface for manipulating {@link Resource}s in the cluster.
 */
@Proxyable
public interface ResourceContext {

    /**
     * Creates a {@link Resource} at the provided {@link Path}.
     *
     * @param path the path
     * @param module the module to instantiate
     * @param args the arguments to pass to the module instantiation
     *
     * @return the system-assigned {@link ResourceId}
     */
    ResourceId create(Path path, String module, Object ... args);

    /**
     * Destroys the {@link Resource} with the provided {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     */
    void destroy(ResourceId resourceId);

    /**
     * Destroys the {@link Resource} using the {@link ResourceId} {@link String}.
     *
     * @param resourceIdString the {@link ResourceId} {@link String}.
     */
    default void destroy(String resourceIdString) {
        destroy(new ResourceId(resourceIdString));
    }

}
