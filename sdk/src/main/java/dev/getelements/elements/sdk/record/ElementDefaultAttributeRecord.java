package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.exception.SdkException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Represents the default value of the attribute.
 *
 * @param name the name of the attribute
 * @param value the defaule value of the attribute
 */
public record ElementDefaultAttributeRecord(String name, String value, String description, boolean sensitive) {

    /**
     * Redacted value for sensitive attributes.
     */
    public static final String REDACTED = "<redacted>";

    /**
     * Constructor with non-null checks.
     * @param name name
     * @param value value
     * @param description description
     */
    public ElementDefaultAttributeRecord {
        name = Objects.requireNonNull(name, "name");
        value = Objects.requireNonNull(value, "value");
        description = Objects.requireNonNull(description, "description");
    }

    /**
     * Gets the ElementDefaultAttributeRecord from the field.
     *
     * @param field the field
     * @return the ElementDefaultAttributeRecord
     */
    public static ElementDefaultAttributeRecord from(final Field field) {

        if (!field.isAnnotationPresent(ElementDefaultAttribute.class)) {
            throw new IllegalArgumentException("Field %s is not annotated with @ElementDefaultAttribute".formatted(field));
        } else if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Field %s is not static".formatted(field));
        }

        try {

            final var name = field.get(null).toString();
            final var annotation = field.getAnnotation(ElementDefaultAttribute.class);

            final var value = annotation
                    .supplier()
                    .getConstructor()
                    .newInstance()
                    .apply(annotation);

            if (value == null) {
                throw new SdkException("Default value for field %s is null".formatted(field));
            }

            return new ElementDefaultAttributeRecord(name, value, annotation.description(), annotation.sensitive());

        } catch (IllegalAccessException |
                 NoSuchMethodException |
                 InstantiationException |
                 InvocationTargetException ex) {
            throw new SdkException(ex);
        }

    }

    public ElementDefaultAttributeRecord redacted() {
        return sensitive() ? new ElementDefaultAttributeRecord(name(), REDACTED, description(), true) : this;
    }

}
