package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;

import java.util.ServiceLoader;

/**
 * Interfaces for a mutable {@link ElementRegistry}. This is a specialization of an {@link ElementRegistry} which
 * permits the loading of additional {@link Element} instances.
 */
public interface MutableElementRegistry extends ElementRegistry {

    /**
     * Using the supplied {@link ElementLoader}, registers an {@link Element} to this instance, making it available to
     * all child and sibling {@link Element}s.
     *
     * @param loader the {@link ElementLoader} to supply the {@link Element}
     */
    default Element register(final ElementLoader loader) {

        final var element = loader.load(this);

        try {
            return register(element);
        } catch (Exception ex) {
            element.close();
            throw ex;
        }

    }

    /**
     * Registers an {@link Element} to this instance, making it available to all child and sibling {@link Element}s.
     *
     * @param loader the {@link ElementLoader} to supply the {@link Element}
     */
    Element register(Element loader);

    /**
     * Unregisters an {@link Element}, provided it was previously registered.
     *
     * @return if the Element existed, and was successfully removed.
     */
    boolean unregister(Element element);

    /**
     * Creates a new instance of the {@link ElementRegistry} using the system default SPI.
     *
     * @return new {@link ElementRegistry}
     */
    static MutableElementRegistry newDefaultInstance() {
        return ServiceLoader
                .load(MutableElementRegistry.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No ElementRegistry SPI Available."))
                .get();
    }

}
