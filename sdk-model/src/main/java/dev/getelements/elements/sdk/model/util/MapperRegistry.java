package dev.getelements.elements.sdk.model.util;

import dev.getelements.elements.sdk.model.exception.MapperException;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Maps instances of one type to another. Originally implemented by Dozer, but now a generic interface due to the
 * deprecation of Dozer.
 */
public interface MapperRegistry {

    /**
     * Gets all the mappers registered in this registry.
     *
     * @return all mappers
     */
    Stream<Mapper<?, ?>> mappers();

    /**
     * Gets all the updaters registered in this registry.
     *
     * @return all updaters
     */
    Stream<Updater<?, ?>> updaters();

    /**
     * Maps the source Object into the supplied destination Object, mutating the destination while preserving the
     * source w/o modification.
     *
     * @param source the source object
     * @param destination the destination object
     */
    default
    <SourceT, DestinationT> void map(final SourceT source, final DestinationT destination) {
        getUpdater(
                (Class<SourceT>) source.getClass(),
                (Class<DestinationT>) destination.getClass()
        ).forward(source, destination);
    }

    /**
     * Maps instances of one type to another.
     *
     * @param source the source type
     * @param destinationClass the destination type
     * @return the destination object
     */
    default
    <SourceT, DestinationT> DestinationT map(
            final SourceT source,
            final Class<DestinationT> destinationClass) {
        return getMapper((Class<SourceT>)source.getClass(), destinationClass).forward(source);
    }

    /**
     * Gets a {@link Mapper}. Throwing {@link MapperException} in the event the mapping isn't found.
     *
     * @param source the source type
     * @param destination the destination type
     * @return the {@link Mapper}
     * @param <SourceT> the source type
     * @param <DestinationT> the destination type
     */
    default <SourceT, DestinationT>
    Mapper<SourceT, DestinationT> getMapper(
            final Class<SourceT> source,
            final Class<DestinationT> destination) {
        return findMapper(source, destination).orElseThrow(() -> new MapperException(
                format("No mapping exists from %s to %s",
                        source.getName(),
                        destination.getName()))
        );
    }

    /**
     * Finds a {@link Mapper}. Throwing {@link MapperException} in the event the mapping isn't found.
     *
     * @param source the source type
     * @param destination the destination type
     * @return the {@link Mapper}
     * @param <SourceT> the source type
     * @param <DestinationT> the destination type
     */
    <SourceT, DestinationT>
    Optional<Mapper<SourceT, DestinationT>> findMapper(Class<SourceT> source, Class<DestinationT> destination);

    /**
     * Gets an {@link Updater}. Throwing {@link MapperException} in the event the mapping isn't found.
     *
     * @param source the source type
     * @param destination the destination type
     * @return the {@link Mapper}
     * @param <SourceT> the source type
     * @param <DestinationT> the destination type
     */
    default <SourceT, DestinationT>
    Updater<SourceT, DestinationT> getUpdater(
            final Class<SourceT> source,
            final Class<DestinationT> destination) {
        return findUpdater(source, destination).orElseThrow(() -> new MapperException(
                format("No mapping exists from %s to %s",
                        source.getName(),
                        destination.getName())
        ));
    }

    /**
     * Finds an {@link Updater}. Throwing {@link MapperException} in the event the mapping isn't found.
     *
     * @param source the source type
     * @param destination the destination type
     * @return the {@link Mapper}
     * @param <SourceT> the source type
     * @param <DestinationT> the destination type
     */
    <SourceT, DestinationT>
    Optional<Updater<SourceT, DestinationT>> findUpdater(Class<SourceT> source, Class<DestinationT> destination);

    @FunctionalInterface
    interface Mapper<SourceT, DestinationT> {

        /**
         * Maps the source to the destination.
         *
         * @param source the source to the destination
         * @return the destination
         */
        DestinationT forward(SourceT source);

        /**
         * Finds the source type. When used as intended, the default implementation should suffice.
         *
         * @return the source type
         */
        default Optional<Class<?>> findSourceType() {

            Class<?> aClass = getClass();
            Optional<Class<?>> result = Optional.empty();

            do {

                var interfaces = aClass.getInterfaces();

                for (int i = 0; i < interfaces.length && result.isEmpty(); i++) {
                    result = findGenericTypeArgumentFromMapper(interfaces[i], 0);
                }

            } while ((aClass = aClass.getSuperclass()) != null && result.isEmpty());

            return result;

        }

        /**
         * Finds the destination type. When used as intended, the default implementation should suffice.
         *
         * @return the destination type.
         */
        default Optional<Class<?>> findDestinationType() {

            Class<?> aClass = getClass();
            Optional<Class<?>> result = Optional.empty();

            do {

                var interfaces = aClass.getInterfaces();

                for (int i = 0; i < interfaces.length && result.isEmpty(); i++) {
                    result = findGenericTypeArgumentFromMapper(interfaces[i], 1);
                }

            } while ((aClass = aClass.getSuperclass()) != null && result.isEmpty());

            return result;

        }

    }

    /**
     * Represents a mapping between two types.
     *
     * @param <SourceT>
     * @param <DestinationT>
     */
    interface ReversibleMapper<SourceT, DestinationT> extends Mapper<SourceT, DestinationT> {

        /**
         * Maps the destination to the source.
         *
         * @param source the source object
         * @return the destination type.
         */
        SourceT reverse(DestinationT source);

        /**
         * Returns this {@link ReversibleMapper}, in reverse.
         *
         * @return this {@link ReversibleMapper}, but in reverse.
         */
        default Mapper<DestinationT, SourceT> reversed() {

            final var _this = this;

            return new Mapper<>() {

                @Override
                public SourceT forward(final DestinationT source) {
                    return _this.reverse(source);
                }

                @Override
                public Optional<Class<?>> findSourceType() {
                    return _this.findDestinationType();
                }

                @Override
                public Optional<Class<?>> findDestinationType() {
                    return _this.findSourceType();
                }

            };

        }


    }

    @FunctionalInterface
    interface Updater<SourceT, DestinationT> {

        /**
         * Maps the source into the existing destination object.
         *
         * @param source the source
         * @param destination the destination
         */
        void forward(SourceT source, DestinationT destination);

        /**
         * Finds the source type. When used as intended, the default implementation should suffice.
         *
         * @return the source type
         */
        default Optional<Class<?>> findSourceType() {

            Class<?> aClass = getClass();
            Optional<Class<?>> result = Optional.empty();

            do {

                var interfaces = aClass.getInterfaces();

                for (int i = 0; i < interfaces.length && result.isEmpty(); i++) {
                    result = findGenericTypeArgumentFromUpdater(interfaces[i], 0);
                }

            } while ((aClass = aClass.getSuperclass()) != null && result.isEmpty());

            return result;

        }

        /**
         * Finds the destination type. When used as intended, the default implementation should suffice.
         *
         * @return the destination type.
         */
        default Optional<Class<?>> findDestinationType() {

            Class<?> aClass = getClass();
            Optional<Class<?>> result = Optional.empty();

            do {

                var interfaces = aClass.getInterfaces();

                for (int i = 0; i < interfaces.length && result.isEmpty(); i++) {
                    result = findGenericTypeArgumentFromUpdater(interfaces[i], 1);
                }

            } while ((aClass = aClass.getSuperclass()) != null && result.isEmpty());

            return result;

        }

    }

    /**
     * Represents a mapping between two types.
     *
     * @param <SourceT>
     * @param <DestinationT>
     */
    interface ReversibleUpdater<SourceT, DestinationT> extends Mapper<SourceT, DestinationT> {

        /**
         * Maps the destination to the source.
         *
         * @param source the source object
         */
        void reverse(DestinationT source, SourceT destination);

        /**
         * Returns this {@link ReversibleMapper}, in reverse.
         *
         * @return this {@link ReversibleMapper}, but in reverse.
         */
        default Updater<DestinationT, SourceT> reversed() {

            final var _this = this;

            return new Updater<>() {

                @Override
                public void forward(final DestinationT destination, final SourceT source) {
                    _this.reverse(destination, source);
                }

                @Override
                public Optional<Class<?>> findSourceType() {
                    return _this.findDestinationType();
                }

                @Override
                public Optional<Class<?>> findDestinationType() {
                    return _this.findSourceType();
                }

            };

        }

    }

    /**
     * Finds the geneirc type argument for the supplied class..
     *
     * @param aClass the class
     * @param index the index
     * @return the generic type argument
     */
    static Optional<Class<?>> findGenericTypeArgumentFromMapper(final Class<?> aClass, final int index) {
        for (var aGenericInterface : aClass.getGenericInterfaces()) {

            if (!(aGenericInterface instanceof ParameterizedType parameterizedType)) {
                continue;
            }

            final var rawType = parameterizedType.getRawType();

            if (!(Mapper.class.equals(rawType) || ReversibleMapper.class.equals(rawType))) {
                continue;
            }

            final var argument = parameterizedType.getActualTypeArguments()[index];

            if (argument instanceof Class<?>) {
                return Optional.of((Class<?>) argument);
            }

        }

        return Optional.empty();

    }

    /**
     * Finds the geneirc type argument for the supplied class..
     *
     * @param aClass the class
     * @param index the index
     * @return the generic type argument
     */
    static Optional<Class<?>> findGenericTypeArgumentFromUpdater(final Class<?> aClass, final int index) {
        for (var aGenericInterface : aClass.getGenericInterfaces()) {

            if (!(aGenericInterface instanceof ParameterizedType parameterizedType)) {
                continue;
            }

            final var rawType = parameterizedType.getRawType();

            if (!(Updater.class.equals(rawType) || ReversibleUpdater.class.equals(rawType))) {
                continue;
            }

            final var argument = parameterizedType.getActualTypeArguments()[index];

            if (argument instanceof Class<?>) {
                return Optional.of((Class<?>) argument);
            }

        }

        return Optional.empty();

    }

}
