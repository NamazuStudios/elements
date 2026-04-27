package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.dao.MorphiaEntityRegistry;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Morphia implementation of {@link ElementEntityRegistrar}.
 *
 * <p>Queries the element's service locator for a {@link MorphiaEntityRegistry}, then calls
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

        locator.findInstance(MorphiaEntityRegistry.class)
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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(final Datastore datastore) {
        this.datastore = datastore;
    }

}
