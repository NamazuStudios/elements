package dev.getelements.elements.sdk.model.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/** A simple {@link MapperRegistry} backed by pre-built maps of {@link Mapper} and {@link Updater} instances. */
public class SimpleMapperRegistry implements MapperRegistry {

    private final Map<MappingKey, Mapper<?,?>> mapperMap;

    private final Map<MappingKey, Updater<?,?>> updatermap;

    /**
     * Creates a new instance with the given mapper and updater maps.
     *
     * @param mapperMap the map of mapping keys to mappers
     * @param updatermap the map of mapping keys to updaters
     */
    public SimpleMapperRegistry(final Map<MappingKey, Mapper<?, ?>> mapperMap,
                                final Map<MappingKey, Updater<?, ?>> updatermap) {
        this.mapperMap = mapperMap;
        this.updatermap = updatermap;
    }

    @Override
    public Stream<Mapper<?, ?>> mappers() {
        return mapperMap.values().stream();
    }

    @Override
    public Stream<Updater<?, ?>> updaters() {
        return updatermap.values().stream();
    }

    @Override
    public <SourceT, DestinationT> Optional<Mapper<SourceT, DestinationT>> findMapper(
            final Class<SourceT> source,
            final Class<DestinationT> destination) {
        final var mapper = mapperMap.get(new MappingKey(source, destination));
        if (mapper != null) {
            return Optional.of((Mapper<SourceT, DestinationT>) mapper);
        }
        return findFromTypeHierarchies(source, destination, mapperMap)
                .map(m -> (Mapper<SourceT, DestinationT>) m);
    }

    @Override
    public <SourceT, DestinationT> Optional<Updater<SourceT, DestinationT>> findUpdater(
            final Class<SourceT> source,
            final Class<DestinationT> destination) {
        final var updater = updatermap.get(new MappingKey(source, destination));
        if (updater != null) {
            return Optional.of((Updater<SourceT, DestinationT>) updater);
        }
        return findFromTypeHierarchies(source, destination, updatermap)
                .map(u -> (Updater<SourceT, DestinationT>) u);
    }

    /**
     * Resolves a mapping when no exact key matches, by walking the source and destination type
     * hierarchies (breadth-first, interfaces before superclasses). Mappings are registered against
     * declared types (e.g. an interface such as {@code RoutingStatus}), while callers hand the
     * registry runtime instances whose classes are concrete implementations -- so the exact lookup
     * alone can never find them.
     *
     * <p>An exact match always wins: this fallback only fires after {@link #findMapper}/
     * {@link #findUpdater} miss. Among hierarchy candidates, the most specific (nearest) source
     * type wins, then the most specific destination type.
     */
    private <M> Optional<M> findFromTypeHierarchies(
            final Class<?> source,
            final Class<?> destination,
            final Map<MappingKey, M> map) {

        for (final var sourceCandidate : typeHierarchyOf(source)) {
            for (final var destinationCandidate : typeHierarchyOf(destination)) {
                if (sourceCandidate == source && destinationCandidate == destination) {
                    continue; // exact pair already tried by the caller
                }
                final var candidate = map.get(new MappingKey(sourceCandidate, destinationCandidate));
                if (candidate != null) {
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();

    }

    /**
     * The type itself first, then its interfaces and superclasses breadth-first, each visited once.
     */
    private static List<Class<?>> typeHierarchyOf(final Class<?> type) {

        if (type == null) {
            return List.of();
        }

        final var ordered = new ArrayList<Class<?>>();
        final var queue = new ArrayDeque<Class<?>>();
        queue.add(type);

        while (!queue.isEmpty()) {

            final var current = queue.poll();

            if (!ordered.add(current)) {
                continue;
            }

            for (final var iface : current.getInterfaces()) {
                queue.add(iface);
            }

            final var superclass = current.getSuperclass();
            if (superclass != null) {
                queue.add(superclass);
            }

        }

        return ordered;

    }

    /**
     * Represents a key for a {@link Mapper} or an {@link Updater}
     *
     * @param source the source type
     * @param destination the destination type
     */
    public record MappingKey(Class<?> source, Class<?> destination) {

        /**
         * Reverses this key.
         *
         * @return the key
         */
        public MappingKey reversed() {
            return new MappingKey(destination(), source());
        }

        /**
         * Get the {@link MappingKey} from the {@link Class} representing the mapper.
         *
         * @param aMapperClass the mapper class
         * @return the {@link MappingKey}
         */
        public static MappingKey fromMapper(final Class<?> aMapperClass) {

            final var source = MapperRegistry.findGenericTypeArgumentFromMapper(aMapperClass, 0)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unable to determine source type for: " +
                            aMapperClass.getName())
                    );

            final var destination = MapperRegistry.findGenericTypeArgumentFromMapper(aMapperClass, 1)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unable to determine destination type for: " +
                            aMapperClass.getName())
                    );

            return new MappingKey(source, destination);

        }

        /**
         * Get the {@link MappingKey} from the {@link Class} representing the updater.
         *
         * @param anUpdaterClass the mapper class
         * @return the {@link MappingKey}
         */
        public static MappingKey fromUpdater(final Class<?> anUpdaterClass) {

            final var source = MapperRegistry.findGenericTypeArgumentFromUpdater(anUpdaterClass, 0)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unable to determine source type for: " +
                            anUpdaterClass.getName())
                    );

            final var destination = MapperRegistry.findGenericTypeArgumentFromUpdater(anUpdaterClass, 1)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unable to determine destination type for: " +
                            anUpdaterClass.getName())
                    );

            return new MappingKey(source, destination);

        }

    }

}
