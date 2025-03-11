package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.exception.InvalidDesignationException;

import java.util.Optional;

import static java.util.regex.Pattern.quote;

/**
 * Parses a designated component. A designated component is one in which there  is a prefix for
 * @param designation
 * @param value
 */
public record ElementDesignationRecord(String designation, String value) {

    public static final String DESIGNATION_SEPARATOR = quote(":");

    /**
     * Parses and, if fails, throw an instance of {@link InvalidDesignationException} if parsing fails.
     *
     * @param value the value
     * @return the record, never null
     * @throws InvalidDesignationException if the designation does not parse.
     */
    public static ElementDesignationRecord parse(final String value) {
        return tryParse(value).orElseThrow(InvalidDesignationException::new);
    }

    /**
     * Tries to parse a designated component.
     * @param value the value
     * @return the {@link Optional} of the parsed value
     */
    public static Optional<ElementDesignationRecord> tryParse(final String value) {
        final var components = value.split(DESIGNATION_SEPARATOR);
        return components.length <= 1
                ? Optional.empty()
                : Optional.of(new ElementDesignationRecord(components[0], value.substring(components[0].length())));

    }

}
