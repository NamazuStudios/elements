package dev.getelements.elements.model.schema;

import io.swagger.annotations.ApiModel;

/**
 * The field type of the field inside MetadataSpec
 */
@ApiModel
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
