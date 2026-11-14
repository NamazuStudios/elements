package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.Element;

import java.util.List;

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
     * Registers entity classes on behalf of an element that has not loaded yet, keyed by the element's
     * implementation classloader (#209). Called in the element's pre-load window: after the classloader
     * exists, but before the element's injector builds, so the shared mapper resolves the new generation's
     * classes while any eager singleton constructs. A later {@link #registerEntityClasses(Element)} call
     * for the same classloader replaces this registration.
     *
     * @param owner the element's implementation classloader; also the deregistration key
     * @param entityClasses the entity classes to register; null or empty removes any existing registration
     *        for the owner
     */
    void registerEntityClasses(ClassLoader owner, List<Class<?>> entityClasses);

    /**
     * Attempts to deregister any entity classes previously registered for the given element.
     *
     * @param element the element whose entity classes should be deregistered
     */
    void unregisterEntityClasses(Element element);

    /**
     * Deregisters any entity classes previously registered for the given classloader — the classloader-keyed
     * counterpart of {@link #unregisterEntityClasses(Element)}. Unlike the element-keyed variant, this must
     * not depend on the (possibly dead) element's service locator still resolving its {@link EntityRegistry}
     * export: a stale generation whose locator no longer resolves must still be removed, or every mapper
     * rebuild keeps re-mapping its classes (#209).
     *
     * @param owner the classloader whose registration should be removed; unknown owners are ignored
     */
    void unregisterEntityClasses(ClassLoader owner);

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
         * Accumulates a classloader-keyed registration to be applied when this batch is closed. See
         * {@link #registerEntityClasses(ClassLoader, List)}.
         *
         * @param owner the element's implementation classloader; also the deregistration key
         * @param entityClasses the entity classes to register; null or empty removes any existing
         *        registration for the owner
         */
        void registerEntityClasses(ClassLoader owner, List<Class<?>> entityClasses);

        /**
         * Accumulates a deregistration to be applied when this batch is closed.
         *
         * @param element the element whose entity classes should be deregistered
         */
        void unregisterEntityClasses(Element element);

        /**
         * Accumulates a classloader-keyed deregistration to be applied when this batch is closed. See
         * {@link #unregisterEntityClasses(ClassLoader)}.
         *
         * @param owner the classloader whose registration should be removed; unknown owners are ignored
         */
        void unregisterEntityClasses(ClassLoader owner);

        /**
         * Applies all accumulated mutations, rebuilding the underlying mapper exactly once.
         */
        @Override
        void close();

    }

}
