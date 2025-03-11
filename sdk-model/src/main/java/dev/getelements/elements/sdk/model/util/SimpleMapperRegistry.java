package dev.getelements.elements.sdk.model.util;

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;

public class SimpleMapperRegistry implements MapperRegistry {

    private final Map<MappingKey, Mapper<?,?>> mapperMap;

    private final Map<MappingKey, Updater<?,?>> updatermap;

    public SimpleMapperRegistry(final Map<MappingKey, Mapper<?, ?>> mapperMap,
                                final Map<MappingKey, Updater<?, ?>> updatermap) {
        this.mapperMap = mapperMap;
        this.updatermap = updatermap;
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

            for (var aGenericInterface : aMapperClass.getGenericInterfaces()) {

                if (!(aGenericInterface instanceof ParameterizedType parameterizedType)) {
                    continue;
                }

                final var rawType = parameterizedType.getRawType();

                if (!(Mapper.class.equals(rawType) || ReversibleMapper.class.equals(rawType))) {
                    continue;
                }

                final var source = parameterizedType.getActualTypeArguments()[0];
                final var destination = parameterizedType.getActualTypeArguments()[1];

                if (source instanceof Class<?> && destination instanceof Class<?>) {
                    return new MappingKey((Class<?>) source, (Class<?>) destination);
                }

            }

            throw new IllegalArgumentException("Unable to determine mapped types for: " + aMapperClass.getName());

        }

        /**
         * Get the {@link MappingKey} from the {@link Class} representing the updater.
         *
         * @param anUpdaterClass the mapper class
         * @return the {@link MappingKey}
         */
        public static MappingKey fromUpdater(final Class<?> anUpdaterClass) {

            for (var aGenericInterface : anUpdaterClass.getGenericInterfaces()) {

                if (!(aGenericInterface instanceof ParameterizedType parameterizedType)) {
                    continue;
                }

                final var rawType = parameterizedType.getRawType();

                if (!(Updater.class.equals(rawType) || ReversibleUpdater.class.equals(rawType))) {
                    continue;
                }

                final var source = parameterizedType.getActualTypeArguments()[0];
                final var destination = parameterizedType.getActualTypeArguments()[1];

                if (source instanceof Class<?> && destination instanceof Class<?>) {
                    return new MappingKey((Class<?>) source, (Class<?>) destination);
                }

            }

            throw new IllegalArgumentException("Unable to determine mapped types for: " + anUpdaterClass.getName());

        }

    }

}
