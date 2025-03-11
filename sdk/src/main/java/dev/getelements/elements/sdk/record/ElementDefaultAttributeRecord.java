package dev.getelements.elements.sdk.record;

import java.util.Objects;

/**
 * Represents the default value of the attribute.
 *
 * @param name the name of the attribute
 * @param value the defaule value of the attribute
 */
public record ElementDefaultAttributeRecord(String name, String value) {
    public ElementDefaultAttributeRecord {
        name = Objects.requireNonNull(name, "name");
        value = Objects.requireNonNull(value, "value");
    }
}
