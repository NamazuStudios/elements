package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InvalidConversionException;

import java.util.List;
import java.util.function.Supplier;

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
     * Gets a listing of simpleResponseHeaderMap mapped by header name.  A header may be repeated and therefore
     * the header may be associated with many values.  If the name appears in the {@link List<String>} returned
     * by {@link #getHeaderNames()}, then this must return a non-null value.
     *
     *
     *
     * @return the mapping of names to values
     */
    List<Object> getHeaders(String name);

    /**
     * Gets the raw values of the specified header.
     *
     * @param header the header name
     * @param defaultListSupplier a {@link Supplier<List<Object>>} to provide the {@link List<Object>} if not present
     * @return the first value of the header, or the default value
     */
    default List<Object> getHeadersOrDefault(final String header, final Supplier<List<Object>> defaultListSupplier) {
        return getHeaderNames().contains(header) ? getHeaders(header) : defaultListSupplier.get();
    }

    /**
     * Gets the raw value of the specified header, fetching the first value in the list
     * of values associated with the header.
     *
     * @param header the header name
     * @return the first value of the header, or null
     */
    default Object getHeader(final String header) {
        return getHeaderOrDefault(header, null);
    }

    /**
     * Gets the raw value of the specified header, fetching the first value in the list
     * of values associated with the header.
     *
     * @param header the header name
     * @param defaultValue the default value to return if the header is not found
     * @return the first value of the header, or null
     */
    default Object getHeaderOrDefault(final String header, final Object defaultValue) {
        final List<Object> headers = getHeaders(header);
        return headers == null || headers.isEmpty() ? defaultValue : headers.get(0);
    }

    /**
     * Gets a single header with the supplied name, attempting to convert the value to the
     * specified type.  The default implementation of this method attempts a simple cast
     * but more sophisticated conversions may be available. This may throw an exception
     * if the type is not convertible.
     *
     * @param header the header
     * @return the header value, or null if no header is found
     * @throws {@link InvalidConversionException} if the conversion is not possible.
     */
    default <T> T getAndConvertHeader(final String header, final Class<T> type) throws InvalidConversionException {

        final Object object = getHeader(header);

        try {
            return type.cast(object);
        } catch (ClassCastException ex) {
            throw new InvalidConversionException(ex);
        }

    }

}
