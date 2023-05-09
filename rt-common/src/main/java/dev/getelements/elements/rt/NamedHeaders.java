package dev.getelements.elements.rt;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Represents a listing of custom named simpleResponseHeaderMap.
 */
public interface NamedHeaders {

    /**
     * The {@link List<String>} of header names available.
     *
     * @return the names of all headers present
     */
    List<String> getHeaderNames();

    /**
     * Gets a listing of headers mapped by header name.  A header may be repeated and therefore there may be many
     * values with a single name.
     *
     * @return a {@link Optional<List<Object>>}.  If {@link #getHeaderNames()} contains the name, then the returned
     *         {@link Optional} must both be present and the enclosed {@link List<Object>} must have at least one value
     */
    Optional<List<Object>> getHeaders(String name);

    /**
     * Gets the raw value of the specified header, fetching the first value in the list
     * of values associated with the header.
     *
     * @param header the header name
     * @return the first value of the header, or null
     */
    default Optional<Object> getHeader(final String header) {
        return getHeaders(header).map(l -> l.get(0));
    }

    /**
     * Gets the raw value of the specified header, fetching the first value in the list
     * of values associated with the header.
     *
     * @param header the header name
     * @param cls the type to attempt casting to
     * @return the first value of the header, or null
     */
    default <T> Optional<T> getHeader(final String header, final Class<T> cls) {
        return getHeaders(header).map(l -> l.get(0)).map(o -> {
            try {
                return cls.cast(o);
            } catch (ClassCastException ex) {
                final Logger logger = getLogger(getClass());
                logger.warn("Expected {} but got {} instead for header {}", cls, o, header);
                return null;
            }
        });
    }

    /**
     * Copies this {@link NamedHeaders} to a {@link Map<String, List<Object>>}
     *
     * @param requestHeaderMap the map to receive the values.
     */
    default void copyToMap(Map<String, List<Object>> requestHeaderMap) {
        for (final String headerName : getHeaderNames()) {
            final Optional<List<Object>> headers = getHeaders(headerName);
            headers.ifPresent(v -> requestHeaderMap.put(headerName, new ArrayList<>(v)));
        }
    }

}
