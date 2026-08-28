package dev.getelements.elements.sdk.address;

import dev.getelements.elements.sdk.ElementRegistry;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represents an address to a specific service within an {@link ElementRegistry}.
 * @param element the {@link ElementAddress}
 * @param type the type of the service
 * @param name the name of the service
 */
public record ElementServiceAddress(ElementAddress element, String type, String name) {

    public ElementServiceAddress {
        requireNonNull(element, "element must not be null");
        requireNonNull(type, "type must not be null");
    }

    /**
     * Builds a method address from the service.
     *
     * @param name the name of the method
     * @param params the parameters
     *
     * @return the {@link ElementMethodAddress}
     */
    public ElementMethodAddress withMethod(final String name, final String ... params) {
        return new ElementMethodAddress(this, name, List.of(params));
    }

}
