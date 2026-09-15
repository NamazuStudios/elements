package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.dao.EntityRegistry;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.morphia.Datastore;
import dev.morphia.mapping.Mapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
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
    public void unregisterUnknownElement_throwsInSingleCallAndBatch() {

        final var unknown = mockElementWithEntity("unknown", DummyEntityA.class);

        assertThrows(IllegalStateException.class, () -> registrar.unregisterEntityClasses(unknown));

        assertThrows(IllegalStateException.class, () -> {
            try (var batch = registrar.beginBatch()) {
                batch.unregisterEntityClasses(unknown);
            }
        });
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

        // Now unregistered, so a second unregister should fail again.
        assertThrows(IllegalStateException.class, () -> registrar.unregisterEntityClasses(elementA));
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
