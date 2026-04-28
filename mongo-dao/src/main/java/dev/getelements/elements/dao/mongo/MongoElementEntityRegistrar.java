package dev.getelements.elements.dao.mongo;

import com.mongodb.client.MongoClient;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.dao.EntityRegistry;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.config.MorphiaConfig;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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

    private Provider<MongoClient> mongoClientProvider;

    private Provider<MorphiaConfig> morphiaConfigProvider;

    private AtomicReference<Datastore> datastoreAtomicReference;

    private final AtomicReference<List<Element>> elementListAtomicReference = new AtomicReference<>(List.of());

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
                    final var datastore = Morphia.createDatastore(
                            getMongoClientProvider().get(),
                            getMorphiaConfigProvider().get()
                    );

                    elementListAtomicReference
                            .updateAndGet(list -> {

                                if (list.contains(element)) {
                                    throw new IllegalStateException("Element already added: " + element
                                            .getElementRecord()
                                            .classLoader()
                                            .getName()
                                    );
                                }

                                return new ArrayList<>(list) { { add(element); }};

                            })
                            .forEach(e -> applyChanges(e, thread, datastore));

                    getDatastoreAtomicReference().set(datastore);

                });
    }

    @Override
    public void unregisterEntityClasses(final Element element) {

        final var locator = element.getServiceLocator();

        locator.findInstance(EntityRegistry.class)
                .map(Supplier::get)
                .ifPresent(registry -> {

                    final var classes = registry.entityClasses();

                    if (classes == null || classes.isEmpty()) {
                        return;
                    }

                    final var thread = Thread.currentThread();
                    final var datastore = Morphia.createDatastore(
                            getMongoClientProvider().get(),
                            getMorphiaConfigProvider().get()
                    );

                    elementListAtomicReference
                            .updateAndGet(list -> {

                                if (list.contains(element)) {
                                    return new ArrayList<>(list) { { remove(element); }};
                                }

                                throw new IllegalStateException("Element not added: " + element
                                        .getElementRecord()
                                        .classLoader()
                                        .getName()
                                );

                            })
                            .forEach(e -> applyChanges(e, thread, datastore));

                    getDatastoreAtomicReference().set(datastore);

                });
    }

    private void applyChanges(final Element element,
                              final Thread thread,
                              final Datastore datastore) {

        final var previous = thread.getContextClassLoader();

        final var elementClassLoader = element
                .getElementRecord()
                .classLoader();

        element
                .getServiceLocator()
                .findInstance(EntityRegistry.class)
                .map(Supplier::get)
                .ifPresent(registry -> {
                    try {
                        thread.setContextClassLoader(elementClassLoader);
                        registry.entityClasses().forEach(entityClass -> {
                            datastore.getMapper().map(entityClass);
                            logger.info(
                                    "Registered Morphia entity class {} for element {}",
                                    entityClass.getName(),
                                    element.getElementRecord().definition().name()
                            );
                        });
                    } finally {
                        thread.setContextClassLoader(previous);
                    }
                });

    }

    public Provider<MongoClient> getMongoClientProvider() {
        return mongoClientProvider;
    }

    @Inject
    public void setMongoClientProvider(Provider<MongoClient> mongoClientProvider) {
        this.mongoClientProvider = mongoClientProvider;
    }

    public Provider<MorphiaConfig> getMorphiaConfigProvider() {
        return morphiaConfigProvider;
    }

    @Inject
    public void setMorphiaConfigProvider(Provider<MorphiaConfig> morphiaConfigProvider) {
        this.morphiaConfigProvider = morphiaConfigProvider;
    }

    public AtomicReference<Datastore> getDatastoreAtomicReference() {
        return datastoreAtomicReference;
    }

    @Inject
    public void setDatastoreAtomicReference(AtomicReference<Datastore> datastoreAtomicReference) {
        this.datastoreAtomicReference = datastoreAtomicReference;
    }

}
