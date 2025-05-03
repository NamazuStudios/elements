package dev.getelements.elements.common.util.mapstruct;

import dev.getelements.elements.sdk.model.exception.MapperException;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.SimpleMapperRegistry;
import dev.getelements.elements.sdk.util.LazyValue;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.String.format;

public class MapstructMapperRegistryBuilder {

    private final Map<Class<?>, Object> cache = new HashMap<>();

    private final Map<Class<?>, Consumer<Object>> creationListeners = new HashMap<>();

    private final LazyValue<ClassGraph> classGraph = new SimpleLazyValue<>(() -> new ClassGraph()
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo());

    public MapstructMapperRegistryBuilder withPackages(final String ... packages) {
        classGraph.get().acceptPackages(packages);
        return this;
    }

    public <CustomT> MapstructMapperRegistryBuilder withCreationListener(
            final Class<CustomT> tClass,
            final Consumer<CustomT> tConsumer) {

        creationListeners.compute(tClass, (k, existing) -> {
            if (existing == null) {
                return (Consumer<Object>) tConsumer;
            } else {
                return existing.andThen((Consumer<Object>)tConsumer);
            }
        });

        return this;

    }

    public MapperRegistry build() {
        final var mappers = buildMappers();
        final var updaters = buildUpdaters();
        return new SimpleMapperRegistry(mappers, updaters);
    }

    private Map<SimpleMapperRegistry.MappingKey, MapperRegistry.Mapper<?,?>> buildMappers() {
        return classGraph.getOptional().map(classGraph -> {
            final var mappers = new LinkedHashMap<SimpleMapperRegistry.MappingKey, MapperRegistry.Mapper<?,?>>();
            scanForImplicitMappers(classGraph, mappers);
            scanForExplicitMappers(classGraph, mappers);
            return mappers;
        }).orElseGet(LinkedHashMap::new);
    }

    private static final String MAPPER_CLASS_NAME = MapperRegistry.Mapper.class.getName();

    private static boolean isImplicitMapper(final ClassInfo aClassInfo) {
        return !isExplicitMapper(aClassInfo);
    }

    private void scanForImplicitMappers(
            final ClassGraph classGraph,
            final Map<SimpleMapperRegistry.MappingKey, MapperRegistry.Mapper<?,?>> mappers) {
        try (var result = classGraph.scan()) {

            result.getClassesWithAnnotation(Mapper.class)
                    .filter(MapstructMapperRegistryBuilder::isImplicitMapper)
                    .stream()
                    .flatMap(anInterface -> anInterface.getMethodInfo().stream())
                    .filter(aMethodInfo -> aMethodInfo.getParameterInfo().length == 1)
                    .filter(aMethodInfo -> !aMethodInfo
                            .getTypeSignatureOrTypeDescriptor()
                            .getResultType()
                            .toString()
                            .equals("void"))
                    .forEach(aMethodInfo -> {

                        final var aMethod = aMethodInfo.loadClassAndGetMethod();

                        final var source = aMethod.getParameterTypes()[0];
                        final var destination = aMethod.getReturnType();

                        final var key = new SimpleMapperRegistry.MappingKey(source, destination);
                        final var instance = getOrCreateMapper(aMethodInfo.getClassInfo().loadClass());

                        final MapperRegistry.Mapper<Object, Object> mapper = object -> {

                            try {
                                return aMethod.invoke(instance, object);
                            } catch (InvocationTargetException ex) {
                                throw new MapperException(ex.getCause());
                            } catch (IllegalAccessException ex) {
                                throw new MapperException(ex);
                            }
                        };

                        if (mappers.putIfAbsent(key, mapper) != null) {
                            throw new MapperException(format(
                                    "Duplicate mapping between %s and %s using %s",
                                    source,
                                    destination,
                                    aMethodInfo.getTypeSignatureOrTypeDescriptor()
                            ));
                        }

                    });

        }
    }

    private static boolean isExplicitMapper(final ClassInfo aClassInfo) {
        return aClassInfo
                .getInterfaces()
                .containsName(MAPPER_CLASS_NAME);
    }

    private void scanForExplicitMappers(
            final ClassGraph classGraph,
            final Map<SimpleMapperRegistry.MappingKey, MapperRegistry.Mapper<?, ?>> mappers) {
        try (var result = classGraph.scan()) {
            result.getClassesWithAnnotation(Mapper.class)
                    .filter(MapstructMapperRegistryBuilder::isExplicitMapper)
                    .stream()
                    .map(ClassInfo::loadClass)
                    .forEach(aMapperInterface -> {

                        final var key = SimpleMapperRegistry.MappingKey.fromMapper(aMapperInterface);
                        final var mapper = (MapperRegistry.Mapper<?, ?>)getOrCreateMapper(aMapperInterface);

                        if (mappers.putIfAbsent(key, mapper) != null) {
                            throw new IllegalArgumentException(format(
                                    "Duplicate mapper between %s and %s using %s.",
                                    key.source(),
                                    key.destination(),
                                    aMapperInterface
                            ));
                        }

                        if (mapper instanceof MapperRegistry.ReversibleMapper<?,?>) {

                            final var reverseKey = key.reversed();
                            final var reverseMapper = ((MapperRegistry.ReversibleMapper<?,?>) mapper).reversed();

                            if (mappers.putIfAbsent(reverseKey, reverseMapper) != null) {
                                throw new IllegalArgumentException(format(
                                        "Duplicate mapper between %s and %s using %s (reversed).",
                                        reverseKey.source(),
                                        reverseKey.destination(),
                                        aMapperInterface
                                ));
                            }

                        }

                    });
        }
    }

    private static final String UPDATER_CLASS_NAME = MapperRegistry.Updater.class.getName();

    private Map<SimpleMapperRegistry.MappingKey, MapperRegistry.Updater<?,?>> buildUpdaters() {
        return classGraph.getOptional().map(classGraph -> {
            final var updaters = new LinkedHashMap<SimpleMapperRegistry.MappingKey, MapperRegistry.Updater<?,?>>();
            scanForImplicitUpdaters(classGraph, updaters);
            scanForExplicitUpdaters(classGraph, updaters);
            return updaters;
        }).orElseGet(LinkedHashMap::new);
    }

    private static boolean isImplicitUpdater(final ClassInfo aClassInfo) {
        return !isExplicitUpdater(aClassInfo);
    }

    private void scanForImplicitUpdaters(
            final ClassGraph classGraph,
            final HashMap<SimpleMapperRegistry.MappingKey, MapperRegistry.Updater<?, ?>> updaters) {
        try (var result = classGraph.scan()) {
            result.getClassesWithAnnotation(Mapper.class)
                    .filter(MapstructMapperRegistryBuilder::isImplicitUpdater)
                    .stream()
                    .flatMap(anInterface -> anInterface.getMethodInfo().stream())
                    .filter(aMethodInfo -> aMethodInfo.getParameterInfo().length == 2)
                    .filter(aMethodInfo -> aMethodInfo.hasParameterAnnotation(MappingTarget.class))
                    .forEach(aMethodInfo -> {

                        final var aMethod = aMethodInfo.loadClassAndGetMethod();

                        final var sourceIndex = aMethodInfo
                                .getParameterInfo()[0]
                                .hasAnnotation(MappingTarget.class) ? 1 : 0;

                        final var destinationIndex = aMethodInfo
                                .getParameterInfo()[0]
                                .hasAnnotation(MappingTarget.class) ? 0 : 1;

                        final var key = new SimpleMapperRegistry.MappingKey(
                                aMethod.getParameterTypes()[sourceIndex],
                                aMethod.getParameterTypes()[destinationIndex]
                        );

                        final var instance = getOrCreateMapper(aMethodInfo.getClassInfo().loadClass());

                        final MapperRegistry.Updater<Object, Object> updater = sourceIndex == 0
                                ? (source, destination) -> {
                            try {
                                aMethod.invoke(instance, source, destination);
                            } catch (InvocationTargetException ex) {
                                throw new MapperException(ex.getCause());
                            } catch (IllegalAccessException ex) {
                                throw new MapperException(ex);
                            }
                        }
                                : (source, destination) -> {
                            try {
                                aMethod.invoke(instance, destination, source);
                            } catch (InvocationTargetException ex) {
                                throw new MapperException(ex.getCause());
                            } catch (IllegalAccessException ex) {
                                throw new MapperException(ex);
                            }
                        };

                        updaters.put(key, updater);

                    });
        }
    }

    private static boolean isExplicitUpdater(final ClassInfo aClassInfo) {
        return aClassInfo
                .getInterfaces()
                .containsName(UPDATER_CLASS_NAME);
    }

    private void scanForExplicitUpdaters(
            final ClassGraph classGraph,
            final HashMap<SimpleMapperRegistry.MappingKey, MapperRegistry.Updater<?, ?>> updaters) {
        try (var result = classGraph.scan()) {
            result.getClassesWithAnnotation(Mapper.class)
                    .filter(MapstructMapperRegistryBuilder::isExplicitUpdater)
                    .stream()
                    .map(ClassInfo::loadClass)
                    .forEach(anUpdaterInterface -> {

                        final var key = SimpleMapperRegistry.MappingKey.fromUpdater(anUpdaterInterface);
                        final var mapper = (MapperRegistry.Updater<?, ?>) getOrCreateMapper(anUpdaterInterface);

                        if (updaters.putIfAbsent(key, mapper) != null) {
                            throw new IllegalArgumentException(format(
                                    "Duplicate updater between %s and %s using %s.",
                                    key.source(),
                                    key.destination(),
                                    anUpdaterInterface
                            ));
                        }

                        if (mapper instanceof MapperRegistry.ReversibleUpdater<?,?>) {

                            final var reverseKey = key.reversed();
                            final var reverseUpdater = ((MapperRegistry.ReversibleUpdater<?,?>) mapper).reversed();

                            if (updaters.putIfAbsent(reverseKey, reverseUpdater) != null) {
                                throw new IllegalArgumentException(format(
                                        "Duplicate mapping between %s and %s using %s (reversed).",
                                        reverseKey.source(),
                                        reverseKey.destination(),
                                        anUpdaterInterface
                                ));
                            }

                        }

                    });
        }
    }

    private Object getOrCreateMapper(final Class<?> aMapperClass) {
        return cache.computeIfAbsent(aMapperClass, _k -> {
            final var mapper = Mappers.getMapper(aMapperClass);
            creationListeners.getOrDefault(aMapperClass, o -> {}).accept(mapper);
            return mapper;
        });
    }

}
