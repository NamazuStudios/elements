package com.namazustudios.socialengine.rt.manifest.model;

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
     * @throws {@link IllegalArgumentException} if the type was not supported
     */
    public static Type findByValue(final String value) {
        return stream(values())
            .filter(t -> t.value.equals(value))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(value + "type not supported."));
    }

}
