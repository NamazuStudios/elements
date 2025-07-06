package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.Attributes;

import java.util.stream.Stream;

/**
 * Represents a local application element record.
 * @param applicationNameOrId the application name or ID
 * @param elementName the package name
 * @param attributes the attributes to use when loading the package
 */
public record ElementsLocalApplicationElementRecord(
        String applicationNameOrId,
        String elementName,
        Attributes attributes) {

    /**
     * Checks if the application name or ID matches any of the provided identifiers.
     * @param identifiers the identifiers to match against
     * @return true if any identifier matches, false otherwise
     */
    public boolean matches(final String ... identifiers) {
        return Stream.of(identifiers).anyMatch(identifier -> identifier.equals(applicationNameOrId()));
    }

}
