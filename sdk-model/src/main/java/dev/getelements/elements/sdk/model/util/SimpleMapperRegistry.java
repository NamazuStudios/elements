package dev.getelements.elements.sdk.model.util;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SimpleMapperRegistry implements MapperRegistry {

    private final Map<MappingKey, Mapper<?,?>> mapperMap;

    private final Map<MappingKey, Updater<?,?>> updatermap;

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
        final var key = new MappingKey(source, destination);
        final var mapper = mapperMap.get(key);
        return Optional.ofNullable((Mapper<SourceT, DestinationT>) mapper);
    }

    @Override
    public <SourceT, DestinationT> Optional<Updater<SourceT, DestinationT>> findUpdater(
            final Class<SourceT> source,
            final Class<DestinationT> destination) {
        final var key = new MappingKey(source, destination);
        final var updater = updatermap.get(key);
        return Optional.ofNullable((Updater<SourceT, DestinationT>) updater);
    }

    /**
     * Represents a key for a {@link Mapper} or an {@link Updater}
     * @param source
     * @param destination
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
