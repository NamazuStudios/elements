package dev.getelements.elements.sdk.util.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Supplies an {@link Optional} for a header type. This presumes that the returned type can be cast to a String. If
 * additional conversion logic is required, then implementations of this type must provide custom logic by overriding
 * the default method {@link #asString(String)}
 *
 * @param <T>
 */
@FunctionalInterface
public interface HeaderOptionalSupplier<T> {

    /**
     * Gets the resulting header value with the supplied name.
     *
     * @param name the name of the header
     *
     * @return the {@link Optional<T>}
     */
    Optional<T> get(String name);

    /**
     * Converts the value to an {@link Optional<String>}
     *
     * @param name the header name
     * @return the value, or null
     */
    default Optional<String> asString(final String name) {
        return get(name).map(value -> {
            try {
                return (String) value;
            } catch (ClassCastException ex) {
                logger().warn("Fetched non-string value for header {}", name);
                return null;
            }
        });
    }

    /**
     * Gets the {@link Logger} used by this instance. By default, this returns the
     *
     * @return the {@link Logger}
     */
    default Logger logger() {
        return LoggerFactory.getLogger(getClass());
    }

}
