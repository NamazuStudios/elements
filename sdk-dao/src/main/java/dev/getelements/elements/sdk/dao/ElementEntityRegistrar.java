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

    /**
     * Begins a batch of {@link #registerEntityClasses(Element)}/{@link #unregisterEntityClasses(Element)}
     * mutations. Mutations made through the returned {@link Batch} are accumulated without rebuilding the
     * underlying mapper; the rebuild happens exactly once, when the batch is closed.
     *
     * <p>Use this instead of individual register/unregister calls when processing several elements
     * together (e.g. all elements in a single deployment), to avoid a full mapper rebuild per element.
     *
     * @return a new {@link Batch}
     */
    Batch beginBatch();

    /**
     * Accumulates {@link #registerEntityClasses(Element)}/{@link #unregisterEntityClasses(Element)}
     * mutations, applying them all at once when closed.
     */
    interface Batch extends AutoCloseable {

        /**
         * Accumulates a registration to be applied when this batch is closed.
         *
         * @param element the element whose entity classes should be registered
         */
        void registerEntityClasses(Element element);

        /**
         * Accumulates a deregistration to be applied when this batch is closed.
         *
         * @param element the element whose entity classes should be deregistered
         */
        void unregisterEntityClasses(Element element);

        /**
         * Applies all accumulated mutations, rebuilding the underlying mapper exactly once.
         */
        @Override
        void close();

    }

}
