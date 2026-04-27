package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.dao.EntityRegistry;
import dev.getelements.elements.sdk.dao.MorphiaEntityRegistry;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Morphia implementation of {@link ElementEntityRegistrar}.
 *
 * <p>Queries the element's service locator for a {@link EntityRegistry}, then calls
 * {@code datastore.getMapper().map(entityClass)} for each declared class with the element's
 * classloader set as the thread context classloader (TCCL). Setting the TCCL ensures Morphia
 * can resolve any reflective helpers (converters, lifecycle callbacks) that live in the element's
 * isolated classloader rather than the platform classloader.
 *
 * <p>Pre-registering entity classes eliminates the {@code DiscriminatorLookup} fallback to
 * {@code Class.forName(String)} (which uses the Morphia library's own classloader and cannot
 * see element classes), removing the need for {@code useDiscriminator = false} on every
 * element-owned {@code @Entity}.
 */
public class MongoElementEntityRegistrar implements ElementEntityRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(MongoElementEntityRegistrar.class);

    private Datastore datastore;

    @Override
    public void registerEntityClasses(final Element element) {

        final var locator = element.getServiceLocator();

        locator.findInstance(EntityRegistry.class)
                .map(supplier -> supplier.get())
                .ifPresent(registry -> {

                    final var classes = registry.entityClasses();

                    if (classes == null || classes.isEmpty()) {
                        return;
                    }

                    final var thread = Thread.currentThread();
                    final var previous = thread.getContextClassLoader();
                    final var elementClassLoader = element.getElementRecord().classLoader();

                    try {
                        thread.setContextClassLoader(elementClassLoader);
                        for (final Class<?> entityClass : classes) {
                            getDatastore().getMapper().map(entityClass);
                            logger.info(
                                    "Registered Morphia entity class {} for element {}",
                                    entityClass.getName(),
                                    element.getElementRecord().definition().name()
                            );
                        }
                    } finally {
                        thread.setContextClassLoader(previous);
                    }

                });
    }

    @Override
    public void unregisterEntityClasses(final Element element) {
        // Morphia's Mapper does not expose a public API for unregistering entity classes.
        // Registered classes remain in the mapper after element unload, but this is harmless:
        // the element's handler chain is removed before this is called, so no new requests
        // will reach element-owned entities. The shadow DiscriminatorLookup uses TCCL as a
        // fallback, which also becomes inert once the element classloader is gone.
        logger.debug("Unregistration not supported by Morphia mapper; skipping for element {}",
                element.getElementRecord().definition().name());
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(final Datastore datastore) {
        this.datastore = datastore;
    }

}
