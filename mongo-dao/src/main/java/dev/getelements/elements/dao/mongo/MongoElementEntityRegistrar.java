package dev.getelements.elements.dao.mongo;

import com.mongodb.client.MongoClient;
import dev.getelements.elements.dao.mongo.migration.PreDatastoreMigrationRunner;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
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
 *
 * <p>Morphia caches entity models by class name. A full datastore rebuild is performed once per
 * {@link #registerEntityClasses(Element)}/{@link #unregisterEntityClasses(Element)} call (or once per
 * {@link Batch}, when several mutations are accumulated via {@link #beginBatch()}) so that reloaded
 * elements (same class name, new classloader) replace the stale cached model rather than being silently
 * ignored. This rebuild replaces (never mutates) the shared {@link Datastore}/{@code Mapper} referenced by
 * {@link #getDatastoreAtomicReference()} — see {@code dev.getelements.elements.sdk.mongo.provider.LiveDatastore}
 * for why a consumer can safely hold the injected {@link Datastore} in a singleton across any number of
 * these rebuilds.
 *
 * <p>Since each rebuild re-maps every element ever registered, callers processing several elements
 * together (e.g. all of a deployment's elements) should use {@link #beginBatch()} rather than calling
 * {@link #registerEntityClasses(Element)}/{@link #unregisterEntityClasses(Element)} once per element, to
 * avoid a full rebuild per element.
 *
 * <p>An internal {@link java.util.concurrent.locks.ReentrantLock} guards the element list and
 * datastore rebuild, held for the duration of a {@link Batch}. This provides a safety net even if the
 * caller does not hold an outer lock.
 */
public class MongoElementEntityRegistrar implements ElementEntityRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(MongoElementEntityRegistrar.class);

    private Provider<MongoClient> mongoClientProvider;

    private Provider<MorphiaConfig> morphiaConfigProvider;

    private Provider<PreDatastoreMigrationRunner> preDatastoreMigrationRunnerProvider;

    private AtomicReference<Datastore> datastoreAtomicReference;

    private final ReentrantLock lock = new ReentrantLock();

    private final List<Element> elements = new ArrayList<>();

    @Override
    public void registerEntityClasses(final Element element) {
        try (var batch = beginBatch()) {
            batch.registerEntityClasses(element);
        }
    }

    @Override
    public void unregisterEntityClasses(final Element element) {
        try (var batch = beginBatch()) {
            batch.unregisterEntityClasses(element);
        }
    }

    @Override
    public Batch beginBatch() {
        return new BatchImpl();
    }

    private void applyRegister(final Element element) {

        final var locator = element.getServiceLocator();

        locator.findInstance(EntityRegistry.class)
                .map(Supplier::get)
                .ifPresent(registry -> {

                    final var classes = registry.entityClasses();

                    if (classes == null || classes.isEmpty()) {
                        return;
                    }

                    if (elements.contains(element)) {
                        throw new IllegalStateException("Element already registered: " +
                                element.getElementRecord().classLoader().getName()
                        );
                    }

                    elements.add(element);

                });

    }

    private void applyUnregister(final Element element) {

        final var locator = element.getServiceLocator();

        locator.findInstance(EntityRegistry.class)
                .map(Supplier::get)
                .ifPresent(registry -> {

                    final var classes = registry.entityClasses();

                    if (classes == null || classes.isEmpty()) {
                        return;
                    }

                    if (!elements.remove(element)) {
                        throw new IllegalStateException("Element not registered: " +
                                element.getElementRecord().classLoader().getName()
                        );
                    }

                });

    }

    /**
     * Accumulates register/unregister mutations while holding {@link #lock}, applying them with a single
     * {@link #rebuildDatastore()} call when closed. Not thread-safe on its own; relies on the enclosing
     * class's lock, which is held for the lifetime of the batch.
     */
    private final class BatchImpl implements Batch {

        private boolean closed = false;

        private BatchImpl() {
            lock.lock();
        }

        @Override
        public void registerEntityClasses(final Element element) {
            ensureOpen();
            applyRegister(element);
        }

        @Override
        public void unregisterEntityClasses(final Element element) {
            ensureOpen();
            applyUnregister(element);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            try {
                rebuildDatastore();
            } finally {
                closed = true;
                lock.unlock();
            }
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException("Batch already closed.");
            }
        }

    }

    private void rebuildDatastore() {

        final var datastore = createDatastore();

        final var thread = Thread.currentThread();
        elements.forEach(e -> applyChanges(e, thread, datastore));

        getDatastoreAtomicReference().set(datastore);

    }

    /**
     * Creates a fresh, empty {@link Datastore}. Exposed as a protected method (rather than inlining the
     * {@link Morphia#createDatastore} call in {@link #rebuildDatastore()}) so tests can override it to avoid
     * requiring a real MongoDB connection.
     */
    protected Datastore createDatastore() {

        final var mongoClient = getMongoClientProvider().get();
        final var morphiaConfig = getMorphiaConfigProvider().get();

        getPreDatastoreMigrationRunnerProvider().get().run(mongoClient, morphiaConfig);

        return Morphia.createDatastore(mongoClient, morphiaConfig);
    }

    private void applyChanges(final Element element,
                              final Thread thread,
                              final Datastore datastore) {

        final var previous = thread.getContextClassLoader();
        final var elementClassLoader = element.getElementRecord().classLoader();

        element.getServiceLocator()
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

    public Provider<PreDatastoreMigrationRunner> getPreDatastoreMigrationRunnerProvider() {
        return preDatastoreMigrationRunnerProvider;
    }

    @Inject
    public void setPreDatastoreMigrationRunnerProvider(
            Provider<PreDatastoreMigrationRunner> preDatastoreMigrationRunnerProvider) {
        this.preDatastoreMigrationRunnerProvider = preDatastoreMigrationRunnerProvider;
    }

    public AtomicReference<Datastore> getDatastoreAtomicReference() {
        return datastoreAtomicReference;
    }

    @Inject
    public void setDatastoreAtomicReference(AtomicReference<Datastore> datastoreAtomicReference) {
        this.datastoreAtomicReference = datastoreAtomicReference;
    }

}