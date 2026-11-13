package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.dao.EntityRegistry;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.morphia.Datastore;
import dev.morphia.mapping.Mapper;
import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertThrows;

public class MongoElementEntityRegistrarTest {

    private CountingRegistrar registrar;

    private AtomicReference<Datastore> datastoreRef;

    private AtomicInteger rebuildCount;

    @BeforeMethod
    public void setUp() {
        datastoreRef = new AtomicReference<>();
        rebuildCount = new AtomicInteger();
        registrar = new CountingRegistrar(rebuildCount);
        registrar.setDatastoreAtomicReference(datastoreRef);
    }

    @Test
    public void registerOneAtATime_rebuildsOncePerElement() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);
        final var elementB = mockElementWithEntity("elementB", DummyEntityB.class);

        registrar.registerEntityClasses(elementA);
        registrar.registerEntityClasses(elementB);

        assertEquals(rebuildCount.get(), 2, "Each individual call should trigger its own rebuild.");
    }

    @Test
    public void registerViaBatch_rebuildsOnce() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);
        final var elementB = mockElementWithEntity("elementB", DummyEntityB.class);

        try (var batch = registrar.beginBatch()) {
            batch.registerEntityClasses(elementA);
            batch.registerEntityClasses(elementB);
        }

        assertEquals(rebuildCount.get(), 1, "A batch of multiple elements should trigger exactly one rebuild.");
    }

    /**
     * Whatever MorphiaConfig an element deployment supplies, the datastore this registrar builds must also
     * map the platform's own DAO model packages: core DAOs decode those types (including legacy rows with
     * embedded discriminators like {@code steps[n]._t: "MongoStep"}) on the shared datastore regardless of
     * which element triggered the build.
     */
    @Test
    public void platformModelPackagesAreAlwaysMergedIntoTheConfig() {

        final var elementOnlyConfig = dev.morphia.config.MorphiaConfig.load()
                .packages(List.of("com.example.element.documents"));

        final var merged = MongoElementEntityRegistrar.withPlatformModelPackages(elementOnlyConfig);

        assertTrue(merged.packages().contains("dev.getelements.elements.dao.mongo.model.*"),
                "the platform model packages must be mapped");
        assertTrue(merged.packages().contains("com.example.element.documents"),
                "element-contributed packages must be preserved");

        // Idempotent: rebuilding from an already-merged config must not duplicate the entry.

        final var mergedAgain = MongoElementEntityRegistrar.withPlatformModelPackages(merged);

        assertEquals(
                mergedAgain.packages().stream()
                        .filter(p -> p.equals("dev.getelements.elements.dao.mongo.model.*"))
                        .count(),
                1,
                "the platform model packages must not be duplicated"
        );

    }

    @Test
    public void duplicateRegistrationMidBatch_throwsButCommitsEarlierMutations() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);

        assertThrows(IllegalStateException.class, () -> {
            try (var batch = registrar.beginBatch()) {
                batch.registerEntityClasses(elementA);
                batch.registerEntityClasses(elementA); // duplicate
            }
        });

        // The batch still committed elementA's registration (via try-with-resources calling close()).
        assertEquals(rebuildCount.get(), 1, "Mutations preceding the failure should still be committed.");
    }

    @Test
    public void unregisterUnknownElement_isIdempotentNoOp() {

        final var unknown = mockElementWithEntity("unknown", DummyEntityA.class);

        // Deregistering an unknown element must not throw: on a hot reload the runtime service unloads the
        // element synchronously (#209) and the container service's event-driven unmount then repeats the call
        // for the same element. The batch still closes (and rebuilds), which is harmless.
        try (var batch = registrar.beginBatch()) {
            batch.unregisterEntityClasses(unknown);
        }

        assertEquals(rebuildCount.get(), 1, "the batch close still rebuilds exactly once");

    }

    @Test
    public void registerThenUnregisterViaBatch_rebuildsOnceAndClearsElement() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);
        registrar.registerEntityClasses(elementA);
        rebuildCount.set(0);

        try (var batch = registrar.beginBatch()) {
            batch.unregisterEntityClasses(elementA);
        }

        assertEquals(rebuildCount.get(), 1);

        // Now unregistered: a second unregister is an idempotent no-op (#209), not a failure.
        try (var batch = registrar.beginBatch()) {
            batch.unregisterEntityClasses(elementA);
        }
    }

    /**
     * Regression test: Morphia's mapper keeps the first model registered per FQCN, so a rebuild must map
     * the most recently registered element's entity classes FIRST. Otherwise, when two live elements
     * declare the same entity class name (e.g. a long-lived deployment still active while a transient one
     * loads), the FQCN binds to the stale element's classloader and a freshly-loaded element resolves the
     * entity through the shared mapper into the old loader's class, crashing its injector with
     * {@code ClassCastException: X cannot be cast to X}.
     */
    @Test
    public void rebuildMapsNewestRegisteredElementsEntityClassesFirst() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);
        final var elementB = mockElementWithEntity("elementB", DummyEntityB.class);

        registrar.registerEntityClasses(elementA);
        registrar.registerEntityClasses(elementB);

        final var mapper = datastoreRef.get().getMapper();

        final InOrder inOrder = inOrder(mapper);
        inOrder.verify(mapper).map(DummyEntityB.class);
        inOrder.verify(mapper).map(DummyEntityA.class);

    }

    private static Element mockElementWithEntity(final String name, final Class<?> entityClass) {

        final var entityRegistry = mock(EntityRegistry.class);
        when(entityRegistry.entityClasses()).thenReturn(List.of(entityClass));

        final var serviceLocator = mock(ServiceLocator.class);
        when(serviceLocator.findInstance(eq(EntityRegistry.class)))
                .thenReturn(Optional.of(() -> entityRegistry));

        final var definition = mock(ElementDefinitionRecord.class);
        when(definition.name()).thenReturn(name);

        final var elementRecord = mock(ElementRecord.class);
        when(elementRecord.definition()).thenReturn(definition);
        when(elementRecord.classLoader()).thenReturn(MongoElementEntityRegistrarTest.class.getClassLoader());

        final var element = mock(Element.class);
        when(element.getServiceLocator()).thenReturn(serviceLocator);
        when(element.getElementRecord()).thenReturn(elementRecord);

        return element;

    }

    private static class DummyEntityA {}

    private static class DummyEntityB {}

    /**
     * Overrides datastore creation to avoid requiring a real MongoDB connection, and counts how many times
     * a rebuild actually happens (i.e. how many fresh datastores get created).
     */
    private static class CountingRegistrar extends MongoElementEntityRegistrar {

        private final AtomicInteger rebuildCount;

        CountingRegistrar(final AtomicInteger rebuildCount) {
            this.rebuildCount = rebuildCount;
        }

        @Override
        protected Datastore createDatastore() {
            rebuildCount.incrementAndGet();
            final var mapper = mock(Mapper.class);
            final var datastore = mock(Datastore.class);
            when(datastore.getMapper()).thenReturn(mapper);
            return datastore;
        }

    }

}
