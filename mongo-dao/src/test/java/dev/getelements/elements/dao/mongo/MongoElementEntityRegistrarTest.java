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

    /**
     * Re-registering the same classloader replaces the registration rather than failing: on a hot reload,
     * pre-registration (#209) lands on the classloader key first, and the element's own
     * {@link EntityRegistry}-driven registration then replaces it with the authoritative class list. The
     * old duplicate-throw behavior would have poisoned exactly that sequence.
     */
    @Test
    public void duplicateRegistrationReplacesRatherThanThrows() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);

        try (var batch = registrar.beginBatch()) {
            batch.registerEntityClasses(elementA);
            batch.registerEntityClasses(elementA); // same classloader: replaces, does not throw
        }

        assertEquals(rebuildCount.get(), 1, "the batch still rebuilds exactly once");

        final var mapper = datastoreRef.get().getMapper();
        verify(mapper, times(1)).map(DummyEntityA.class);

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

    /**
     * THE rc-7 regression (#209): a generation whose runtime was already torn down can no longer resolve its
     * {@link EntityRegistry} export through its service locator. The old deregistration gate resolved the
     * registry <i>before</i> removing anything, so such a generation was silently never removed — it stayed
     * in the registration list and every subsequent mapper rebuild kept re-mapping its closed classes.
     * Deregistration is now keyed by classloader and unconditional.
     */
    @Test
    public void unregisterRemovesDeadGenerationWhoseLocatorCannotResolveRegistry() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);
        registrar.registerEntityClasses(elementA);
        rebuildCount.set(0);

        // The same generation, now dead: SAME classloader, locator no longer resolves the registry.
        final var dead = mockDeadElementWithEntity("elementA", DummyEntityA.class,
                elementA.getElementRecord().classLoader());
        try (var batch = registrar.beginBatch()) {
            batch.unregisterEntityClasses(dead);
        }

        assertEquals(rebuildCount.get(), 1, "the batch close rebuilds exactly once");

        // The rebuild must no longer map the dead generation's classes. Each rebuild builds a fresh
        // mapper, so the current datastore's mapper must have received no map() calls at all.
        final var mapper = datastoreRef.get().getMapper();
        verify(mapper, never()).map(DummyEntityA.class);

    }

    /**
     * Pre-registration (#209) happens in the element's pre-load window, keyed by the element's impl
     * classloader; the element's own {@link EntityRegistry}-driven registration then replaces it once the
     * element has loaded. Both must land on the same key, and the second registration must not double-map
     * the classes.
     */
    @Test
    public void preRegistrationByClassloaderIsReplacedByElementRegistration() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);
        final var classLoader = elementA.getElementRecord().classLoader();

        // 1. Pre-load window: classes registered by classloader, before the injector builds.
        registrar.registerEntityClasses(classLoader, List.of(DummyEntityA.class));
        // 2. Post-load: the element's EntityRegistry registration replaces the pre-registration.
        registrar.registerEntityClasses(elementA);

        assertEquals(rebuildCount.get(), 2, "each registration call rebuilds once");

        // Each rebuild builds a FRESH mapper, so the final mapper must show exactly one map() call for the
        // second rebuild -- proving the replacement registration mapped the class once, not twice.
        final var mapper = datastoreRef.get().getMapper();
        verify(mapper, times(1)).map(DummyEntityA.class);

    }

    /**
     * The classloader-keyed deregistration removes a pre-registration directly — used on the failed-element
     * path so a load failure does not pin the failed generation's classloader in the registrar.
     */
    @Test
    public void preRegistrationIsRemovedByClassloaderKeyedDeregistration() {

        final var elementA = mockElementWithEntity("elementA", DummyEntityA.class);
        final var classLoader = elementA.getElementRecord().classLoader();

        registrar.registerEntityClasses(classLoader, List.of(DummyEntityA.class));
        rebuildCount.set(0);

        registrar.unregisterEntityClasses(classLoader);

        assertEquals(rebuildCount.get(), 1);

        final var mapper = datastoreRef.get().getMapper();
        verify(mapper, never()).map(DummyEntityA.class);

    }

    private static Element mockElementWithEntity(final String name, final Class<?> entityClass) {

        final var entityRegistry = mock(EntityRegistry.class);
        when(entityRegistry.entityClasses()).thenReturn(List.of(entityClass));

        final var serviceLocator = mock(ServiceLocator.class);
        when(serviceLocator.findInstance(eq(EntityRegistry.class)))
                .thenReturn(Optional.of(() -> entityRegistry));

        return mockElement(name, serviceLocator, entityClass);

    }

    /**
     * An element whose runtime is already torn down: its service locator no longer resolves the
     * {@link EntityRegistry} export. This is the shape of a stale generation at deregister time on a hot
     * reload (#209) — deregistration must still remove the element's classes. Shares the SAME classloader
     * as the original generation: the dead generation differs only in that its locator no longer resolves.
     */
    private static Element mockDeadElementWithEntity(final String name,
                                                     final Class<?> entityClass,
                                                     final ClassLoader classLoader) {

        final var serviceLocator = mock(ServiceLocator.class);
        when(serviceLocator.findInstance(eq(EntityRegistry.class))).thenReturn(Optional.empty());

        return mockElement(name, serviceLocator, entityClass, classLoader);

    }

    private static Element mockElement(final String name, final ServiceLocator serviceLocator, final Class<?> entityClass) {

        // Each element gets its OWN classloader mock: real elements never share an implementation
        // classloader, and the registrar keys registrations by classloader.
        final var classLoader = mock(ClassLoader.class);
        when(classLoader.getName()).thenReturn("ELEMENTPATH[" + name + "]");

        return mockElement(name, serviceLocator, entityClass, classLoader);

    }

    private static Element mockElement(final String name,
                                       final ServiceLocator serviceLocator,
                                       final Class<?> entityClass,
                                       final ClassLoader classLoader) {

        final var definition = mock(ElementDefinitionRecord.class);
        when(definition.name()).thenReturn(name);

        final var elementRecord = mock(ElementRecord.class);
        when(elementRecord.definition()).thenReturn(definition);
        when(elementRecord.classLoader()).thenReturn(classLoader);

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
