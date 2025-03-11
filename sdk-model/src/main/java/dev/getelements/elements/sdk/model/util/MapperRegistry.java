package dev.getelements.elements.sdk.model.util;

import dev.getelements.elements.sdk.model.exception.MapperException;

import java.util.Optional;

import static java.lang.String.format;

/**
 * Maps instances of one type to another. Originally implemented by Dozer, but now a generic interface due to the
 * deprecation of Dozer.
 */
public interface MapperRegistry {

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
            return ReversibleMapper.this::reverse;
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
            return ReversibleUpdater.this::reverse;
        }

    }

}
