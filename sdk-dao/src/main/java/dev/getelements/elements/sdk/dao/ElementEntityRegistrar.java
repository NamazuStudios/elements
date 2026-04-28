package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.Element;

/**
 * Platform service that discovers {@link EntityRegistry} implementations exported by an
 * {@link Element} and registers the declared entity classes with the underlying database mapper.
 *
 * <p>This is an optional binding in non-Mongo deployments; the loader injects it with
 * {@code @Inject(optional = true)} and skips registration silently when no implementation
 * is bound.
 */
public interface ElementEntityRegistrar {

    /**
     * Discovers all {@link EntityRegistry} exports from the element's service locator
     * and registers their entity classes with the mapper before the element starts serving requests.
     *
     * @param element the element whose entity classes should be registered
     */
    void registerEntityClasses(Element element);

    /**
     * Attempts to deregister any entity classes previously registered for the given element.
     * Implementations should make a best effort to clean up mapper state, but may no-op if the
     * underlying mapper does not support unregistration.
     *
     * @param element the element whose entity classes should be deregistered
     */
    void unregisterEntityClasses(Element element);

}
