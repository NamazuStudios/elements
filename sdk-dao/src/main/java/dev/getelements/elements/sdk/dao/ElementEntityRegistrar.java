package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.Element;

/**
 * Platform service that discovers {@link MorphiaEntityRegistry} implementations exported by an
 * {@link Element} and registers the declared entity classes with the underlying database mapper.
 *
 * <p>This is an optional binding in non-Mongo deployments; {@code JakartaRsLoader} injects it
 * with {@code @Inject(optional = true)} and skips registration silently when no implementation
 * is bound.
 */
public interface ElementEntityRegistrar {

    /**
     * Discovers all {@link MorphiaEntityRegistry} exports from the element's service locator
     * and registers their entity classes with the mapper before the element's REST context starts.
     *
     * @param element the element whose entity classes should be registered
     */
    void registerEntityClasses(Element element);

}
