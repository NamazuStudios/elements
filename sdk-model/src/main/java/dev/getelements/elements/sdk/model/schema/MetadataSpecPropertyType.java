package dev.getelements.elements.sdk.model.schema;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The field type of the field inside MetadataSpec
 */
@Schema
public enum MetadataSpecPropertyType {

    /**
     * String type.
     */
    STRING,

    /**
     * Enum type.
     */
    NUMBER,

    /**
     * Boolean type.
     */
    BOOLEAN,

    /**
     * Array type.
     */
    ARRAY,

    /**
     * Enumeration type.
     */
    ENUM,

    /**
     * Object type.
     */
    OBJECT,

    /**
     * Tags type
     */
    TAGS

}
