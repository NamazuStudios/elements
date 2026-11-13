package dev.getelements.elements.sdk.model.util;

import dev.getelements.elements.sdk.model.exception.MapperException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class SimpleMapperRegistryTest {

    private Map<SimpleMapperRegistry.MappingKey, MapperRegistry.Mapper<?, ?>> mapperMap;

    private Map<SimpleMapperRegistry.MappingKey, MapperRegistry.Updater<?, ?>> updaterMap;

    private SimpleMapperRegistry registry;

    @BeforeMethod
    public void setUp() {
        mapperMap = new LinkedHashMap<>();
        updaterMap = new LinkedHashMap<>();
        registry = new SimpleMapperRegistry(mapperMap, updaterMap);
    }

    /**
     * An exact registered pair must always win over any hierarchy-based candidate.
     */
    @Test
    public void exactMatchWinsOverHierarchy() {

        mapperMap.put(key(SourceInterface.class, Target.class), forwardTarget("exact"));
        mapperMap.put(key(Object.class, Target.class), forwardTarget("hierarchy"));

        final var result = registry.map(new SourceImpl(), Target.class);

        assertEquals(result.value, "exact");

    }

    /**
     * Mappings are registered against declared types (interfaces), while callers pass runtime
     * instances whose classes are concrete implementations. The lookup must walk the source type
     * hierarchy to find them.
     */
    @Test
    public void sourceInterfaceIsResolvedFromRuntimeClass() {

        mapperMap.put(key(SourceInterface.class, Target.class), forwardTarget("via-interface"));

        final var result = registry.map(new SourceImpl(), Target.class);

        assertEquals(result.value, "via-interface");

    }

    /**
     * The most specific (nearest) source type wins when several hierarchy candidates are registered.
     */
    @Test
    public void mostSpecificSourceCandidateWins() {

        mapperMap.put(key(Object.class, Target.class), forwardTarget("via-object"));
        mapperMap.put(key(SourceSuperclass.class, Target.class), forwardTarget("via-superclass"));

        final var result = registry.map(new SourceImpl(), Target.class);

        assertEquals(result.value, "via-superclass");

    }

    /**
     * The destination hierarchy is walked too: a caller passing a destination subclass must find a
     * mapping registered against the destination superclass.
     */
    @Test
    public void destinationSuperclassIsResolvedFromRuntimeClass() {

        mapperMap.put(key(SourceInterface.class, TargetSuperclass.class), forwardTargetSubclass("via-destination"));

        final var mapper = registry
                .<SourceInterface, TargetSubclass>getMapper(SourceInterface.class, TargetSubclass.class);

        assertEquals(mapper.forward(new SourceImpl()).value, "via-destination");

    }

    /**
     * Updaters resolve through the same hierarchy walk as mappers.
     */
    @Test
    public void updaterResolvesThroughSourceHierarchy() {

        updaterMap.put(key(SourceInterface.class, TargetSuperclass.class), updater("updated"));

        final var target = new TargetSubclass();
        registry.map(new SourceImpl(), target);

        assertEquals(target.value, "updated");

    }

    /**
     * No hierarchy candidate at all must still throw the familiar MapperException.
     */
    @Test
    public void unknownMappingStillThrowsMapperException() {
        assertThrows(MapperException.class, () -> registry.map(new SourceImpl(), Target.class));
    }

    private SimpleMapperRegistry.MappingKey key(final Class<?> source, final Class<?> destination) {
        return new SimpleMapperRegistry.MappingKey(source, destination);
    }

    private MapperRegistry.Mapper<SourceInterface, Target> forwardTarget(final String value) {
        return source -> {
            final var target = new Target();
            target.value = value;
            return target;
        };
    }

    @SuppressWarnings("unchecked")
    private MapperRegistry.Mapper<SourceInterface, TargetSuperclass> forwardTargetSubclass(final String value) {
        return source -> {
            final var target = new TargetSubclass();
            target.value = value;
            return target;
        };
    }

    private MapperRegistry.Updater<SourceInterface, TargetSuperclass> updater(final String value) {
        return (source, target) -> target.value = value;
    }

    interface SourceInterface {}

    abstract static class SourceSuperclass implements SourceInterface {}

    static class SourceImpl extends SourceSuperclass {}

    static class TargetSuperclass {
        String value;
    }

    static class Target extends TargetSuperclass {}

    static class TargetSubclass extends TargetSuperclass {}

}
