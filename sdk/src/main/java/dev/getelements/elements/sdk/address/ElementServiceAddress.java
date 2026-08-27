package dev.getelements.elements.sdk.address;

import dev.getelements.elements.sdk.ElementRegistry;

import java.util.List;

/**
 * Represents an address to a specific service within an {@link ElementRegistry}.
 * @param element the {@link ElementAddress}
 * @param type the type of the service
 * @param name the name of the service
 */
public record ElementServiceAddress(ElementAddress element, String type, String name) {

    /**
     * Builds a method address
     *
     * @param name the name
     * @param params the parameters
     *
     * @return the {@link ElementMethodAddress}
     */
    public ElementMethodAddress withMethod(String name, String ... params) {
        return new ElementMethodAddress(this, name, List.of(params));
    }

}
