package dev.getelements.elements.sdk.address;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.record.ElementServiceKey;

/**
 * Defines an {@link ElementAddress} which is simply an object referencing an {@link Element} within an
 * {@link ElementRegistry}.
 *
 * @param name the name of the element
 * @param index the index the element within the registry. If <0 then assume default index
 */
public record ElementAddress(String name, int index) {

    /**
     * Indicates the default index
     */
    public static final int DEFAULT_INDEX = -1;

    /**
     * Create an {@link ElementAddress} with just the name and default index
     * @param name the name
     */
    public ElementAddress(String name) {
        this(name, DEFAULT_INDEX);
    }

    /**
     * True if the index is default.
     *
     * @return if the index is default
     */
    public boolean isDefault() {
        return index() == DEFAULT_INDEX;
    }

    /**
     * Specifies the service within the {@link Element}.
     *
     * @param serviceKey the service key
     * @return the {@link ElementServiceKey}
     * @param <ServiceT> the service type
     */
    public <ServiceT> ElementServiceAddress withService(final ElementServiceKey<ServiceT> serviceKey) {
        return new ElementServiceAddress(this, serviceKey.type().getName(), serviceKey.name());
    }

    /**
     * Specifies the service within the {@link Element}.
     *
     * @param type the type
     * @return the {@link ElementServiceKey}
     */
    public ElementServiceAddress withService(final String type) {
        return new ElementServiceAddress(this, type, null);
    }

    /**
     * Specifies the service within the {@link Element}.
     *
     * @param type the type
     * @param name the name of the service
     * @return the {@link ElementServiceKey}
     */
    public ElementServiceAddress withService(final String type, final String name) {
        return new ElementServiceAddress(this, type, name);
    }

}
