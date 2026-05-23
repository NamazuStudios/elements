package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementRequiredAttribute;
import dev.getelements.elements.sdk.exception.SdkException;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Represents a required attribute declared via {@link ElementRequiredAttribute}. Unlike
 * {@link ElementDefaultAttributeRecord}, this record carries no default value - it solely signals to operators
 * that the attribute must be configured at deployment time.
 *
 * @param name        the attribute key (the value of the annotated static field)
 * @param description a human-readable description of the attribute
 * @param sensitive   whether the attribute value should be masked in management UIs
 */
public record ElementRequiredAttributeRecord(String name, String description, boolean sensitive) {

    public ElementRequiredAttributeRecord {
        name = Objects.requireNonNull(name, "name");
        description = Objects.requireNonNull(description, "description");
    }

    /**
     * Constructs an {@link ElementRequiredAttributeRecord} from a field bearing {@link ElementRequiredAttribute}.
     *
     * @param field the field
     * @return the record
     */
    public static ElementRequiredAttributeRecord from(final Field field) {

        if (!field.isAnnotationPresent(ElementRequiredAttribute.class)) {
            throw new IllegalArgumentException(
                    "Field %s is not annotated with @ElementRequiredAttribute".formatted(field));
        } else if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Field %s is not static".formatted(field));
        }

        try {
            final var name = field.get(null).toString();
            final var annotation = field.getAnnotation(ElementRequiredAttribute.class);
            return new ElementRequiredAttributeRecord(name, annotation.description(), annotation.sensitive());
        } catch (IllegalAccessException ex) {
            throw new SdkException(ex);
        }

    }

}
