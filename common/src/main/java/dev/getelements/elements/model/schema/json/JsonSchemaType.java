package dev.getelements.elements.model.schema.json;

import dev.getelements.elements.model.schema.MetadataSpecPropertyType;

import java.util.*;

/**
 * Constants for the JSON Schema type.
 */
public class JsonSchemaType {

    private JsonSchemaType() {}

    /**
     * String type.
     */
    public static final String STRING = "string";

    /**
     * Enum type.
     */
    public static final String NUMBER = "number";

    /**
     * Boolean type.
     */
    public static final String BOOLEAN = "boolean";

    /**
     * Array type.
     */
    public static final String ARRAY = "array";

    /**
     * Array type.
     */
    public static final String OBJECT = "object";

    /**
     * Null Type.
     */
    public static final String NULL = "null";

    private static Map<MetadataSpecPropertyType, String> jsonSchemaTypes;

    static {
        final Map<MetadataSpecPropertyType, String> jsonSchemaTypes = new EnumMap<>(MetadataSpecPropertyType.class);
        jsonSchemaTypes.put(MetadataSpecPropertyType.STRING, STRING);
        jsonSchemaTypes.put(MetadataSpecPropertyType.NUMBER, NUMBER);
        jsonSchemaTypes.put(MetadataSpecPropertyType.BOOLEAN, BOOLEAN);
        jsonSchemaTypes.put(MetadataSpecPropertyType.ARRAY, ARRAY);
        jsonSchemaTypes.put(MetadataSpecPropertyType.OBJECT, OBJECT);
        jsonSchemaTypes.put(MetadataSpecPropertyType.TAGS, ARRAY);
        jsonSchemaTypes.put(MetadataSpecPropertyType.ENUM, STRING);
        JsonSchemaType.jsonSchemaTypes = Collections.unmodifiableMap(jsonSchemaTypes);
    }

    private static Map<String, MetadataSpecPropertyType> metadataSpecPropertyTypes;

    static {
        final Map<String, MetadataSpecPropertyType> metadataSpecPropertyTypes = new LinkedHashMap<>();
        metadataSpecPropertyTypes.put(STRING, MetadataSpecPropertyType.STRING);
        metadataSpecPropertyTypes.put(NUMBER, MetadataSpecPropertyType.NUMBER);
        metadataSpecPropertyTypes.put(BOOLEAN, MetadataSpecPropertyType.BOOLEAN);
        metadataSpecPropertyTypes.put(ARRAY, MetadataSpecPropertyType.ARRAY);
        metadataSpecPropertyTypes.put(OBJECT, MetadataSpecPropertyType.OBJECT);
        JsonSchemaType.metadataSpecPropertyTypes = Collections.unmodifiableMap(metadataSpecPropertyTypes);
    }

    private static final Set<String> types = Set.of(STRING, NUMBER, BOOLEAN, ARRAY, OBJECT, NULL);

    /**
     * Gets all valid JSON Schema Types.
     *
     * @return an immutable {@link Set} of json schmea types.
     */
    public static Set<String> getJsonSchemaTypes() {
        return types;
    }

    /**
     * Finds the JSON Schema Type from the supplied {@link MetadataSpecPropertyType}.
     *
     * @param metadataSpecPropertyType the metadata spec type
     *
     * @return an {@link Optional} containing the type
     */
    public static Optional<String> findJsonSchemaType(final MetadataSpecPropertyType metadataSpecPropertyType) {
        return Optional.ofNullable(jsonSchemaTypes.get(metadataSpecPropertyType));
    }

    /**
     * Gets the JSON Schema Type from the supplied {@link MetadataSpecPropertyType}.
     *
     * @param metadataSpecPropertyType the metadata spec type, never null
     * @throws {@link IllegalArgumentException} if the type wasn't found.
     *
     * @return an {@link Optional} containing the type
     */
    public static String getJsonSchemaType(final MetadataSpecPropertyType metadataSpecPropertyType) {
        return findJsonSchemaType(metadataSpecPropertyType).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Gets the {@link MetadataSpecPropertyType}
     *
     * @param jsonSchemaType the JSON Schema Type
     * @return an {@link Optional} of the {@link MetadataSpecPropertyType}
     */
    public static Optional<MetadataSpecPropertyType> findMetadataSpecPropertyType(final String jsonSchemaType) {
        return Optional.ofNullable(metadataSpecPropertyTypes.get(jsonSchemaType));
    }

    /**
     * Gets the {@link MetadataSpecPropertyType}
     *
     * @param jsonSchemaType the JSON Schema Type
     * @return an {@link Optional} of the {@link MetadataSpecPropertyType}
     */
    public static MetadataSpecPropertyType getMetadataSpecPropertyType(final String jsonSchemaType) {
        return findMetadataSpecPropertyType(jsonSchemaType).orElseThrow(IllegalArgumentException::new);
    }

}
