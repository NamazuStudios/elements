package dev.getelements.elements.rt.manifest.model;

import java.util.Collection;

import static dev.getelements.elements.rt.Reflection.*;
import static java.util.Arrays.stream;

/**
 * The listing of pre-defined types in the {@link ModelManifest}.
 *
 * Created by patricktwohig on 8/16/17.
 */
public enum Type {

    /**
     * The string type.  Generally expressed as a {@link String}.
     */
    STRING("string"),

    /**
     * The number type.  Generally expressed as a {@link Double} or primitive Java double.
     */
    NUMBER("number"),

    /**
     * The number type.  Generally expressed as a {@link Integer} or primitive Java double.
     */
    INTEGER("integer"),

    /**
     * The boolean type.  Generally expressed as a {@link Boolean} or primitive Java boolean.
     */
    BOOLEAN("boolean"),

    /**
     * The object type.
     */
    OBJECT("object"),

    /**
     * The array type.
     */
    ARRAY("array");

    public final String value;

    Type(final String value) {
        this.value = value;
    }

    /**
     * Finds the {@link Type} instance by the value read from the underlying script.
     *
     * @param value the value
     * @return the {@link Type} instance
     *
     * @throws IllegalArgumentException if the type was not supported
     */
    public static Type findByValue(final String value) {
        return stream(values())
            .filter(t -> t.value.equals(value))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(value + "type not supported."));
    }

    public static Type findByClass(final Class<?> cls) {
        if (String.class.equals(cls)) {
            return STRING;
        } else if (isObjectFloat(cls) || isPrimitiveFloat(cls)) {
            return NUMBER;
        } else if (isObjectInteger(cls) || isPrimitiveInteger(cls)) {
            return INTEGER;
        } else if (Boolean.class.equals(cls) || boolean.class.equals(cls)) {
            return BOOLEAN;
        } else if (Collection.class.isAssignableFrom(cls) || cls.isArray()) {
            return ARRAY;
        } else {
            return OBJECT;
        }
    }

}
