package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkServiceNotFoundException;
import dev.getelements.elements.sdk.record.ElementServiceKey;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Used to access instances which would otherwise be provided using the javax.getInstance annotations javax.injext.Inject and
 * javax.injext.Named.
 *
 * Created by patricktwohig on 8/27/15.
 */
public interface ServiceLocator {

    /**
     * Gets the type of service, unnamed.
     *
     * @param tClass the type to getInstance
     * @param <T> the type ot getInstance
     * @return an injected instance of the supplied {@link Class<T>}
     */
    default <T> T getInstance(final Class<T> tClass) {
        return findInstance(tClass)
                .orElseThrow(SdkServiceNotFoundException::new)
                .get();
    }

    /**
     * Gets the type of the service with the specific name.
     *
     * @param tClass the type
     * @param named the name as represented by the javax.injext.Named annotation
     * @param <T>
     *
     * @return the object representing the requested type.
     */
    default <T> T getInstance(final Class<T> tClass, final String named) {
        return findInstance(tClass, named)
                .orElseThrow(SdkServiceNotFoundException::new)
                .get();
    }

    /**
     * Gets the type of the service with the specific name.
     *
     * @param serviceKey the {@link ElementServiceKey}
     * @param <T> the type
     *
     * @return the object representing the requested type.
     */
    default <T> T getInstance(final ElementServiceKey<T> serviceKey) {
        return findInstance(serviceKey)
                .orElseThrow(SdkServiceNotFoundException::new)
                .get();
    }

    /**
     * Finds the {@link Supplier} to the supplied instance. This does not load the instance until the returned
     * {@link Supplier} is called, therefore avoiding expensive loading operations if just checking for the existence
     * of the service.
     *
     * @param tClass the type to fine
     * @return an {@link Optional} of {@link Supplier} to the object
     * @param <T> the type to request
     */
    default <T> Optional<Supplier<T>> findInstance(final Class<T> tClass) {
        final var key = new ElementServiceKey<>(tClass, null);
        return findInstance(key);
    }

    /**
     * Finds the {@link Supplier} to the supplied instance. This does not load the instance until the returned
     * {@link Supplier} is called, therefore avoiding expensive loading operations if just checking for the existence
     * of the service.
     *
     * @param tClass the type to fine
     * @param named the name of the service
     * @return an {@link Optional} of {@link Supplier} to the object
     * @param <T> the type to request
     */
    default <T> Optional<Supplier<T>> findInstance(final Class<T> tClass, final String named) {
        final var key = new ElementServiceKey<>(tClass, named);
        return findInstance(key);
    }

    /**
     * Gets the service using the {@link ElementServiceKey}.
     *
     * @param key the key
     * @return an {@link Optional} of {@link Supplier} to the object
     * @param <T> the type to request
     */
    <T> Optional<Supplier<T>> findInstance(ElementServiceKey<T> key);

}
