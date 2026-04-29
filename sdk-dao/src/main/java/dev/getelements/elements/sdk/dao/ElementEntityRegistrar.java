package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.Element;

/**
 * Platform service that discovers {@link EntityRegistry} implementations exported by an
 * {@link Element} and registers the declared entity classes with the underlying database mapper.
 *
 * <p>Implementations are bound by the database module (e.g. {@code MongoDaoModule}) and
 * exposed to the root injector by the corresponding element module (e.g. {@code MongoDaoElementModule}).
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
     *
     * @param element the element whose entity classes should be deregistered
     */
    void unregisterEntityClasses(Element element);

}
