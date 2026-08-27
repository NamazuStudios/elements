package dev.getelements.elements.sdk.address;

import java.lang.reflect.Method;
import java.util.List;

/**
 * References a particular {@link Method} within a specific Element and service.
 *
 * @param service the service address
 * @param name the name of the method
 * @param parameters the parameters of the method as fully qualified java classnames
 */
public record ElementMethodAddress(
        ElementServiceAddress service,
        String name,
        List<String> parameters) {

    public ElementMethodAddress {
        parameters = List.copyOf(parameters);
    }

}
