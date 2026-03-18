package dev.getelements.elements.sdk.model.schema.json;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Represents a subset of the JSON Schema specification. */
@Schema(description = "A Subset of the JSON-Schema - https://json-schema.org/draft/2020-12/json-schema-core")
public class JsonSchema {

    /** Creates a new instance. */
    public JsonSchema() {}

    /** The URI for the JSON Schema draft 2020-12 specification. */
    public static final String SCHEMA_DRAFT_2020_12 = "https://json-schema.org/draft/2020-12/schema";

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String $id;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String $schema = SCHEMA_DRAFT_2020_12;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String title;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String description;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String type;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private Map<String, JsonSchemaProperty> properties;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private List<String> required;

    /**
     * Returns the schema ID.
     *
     * @return the schema ID
     */
    public String get$id() {
        return $id;
    }

    /**
     * Sets the schema ID.
     *
     * @param $id the schema ID
     */
    public void set$id(String $id) {
        this.$id = $id;
    }

    /**
     * Returns the schema URI.
     *
     * @return the schema URI
     */
    public String get$schema() {
        return $schema;
    }

    /**
     * Sets the schema URI.
     *
     * @param $schema the schema URI
     */
    public void set$schema(String $schema) {
        this.$schema = $schema;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the properties map.
     *
     * @return the properties
     */
    public Map<String, JsonSchemaProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the properties map.
     *
     * @param properties the properties
     */
    public void setProperties(Map<String, JsonSchemaProperty> properties) {
        this.properties = properties;
    }

    /**
     * Populates properties from the given map.
     *
     * @param properties the source properties map
     */
    public void fromProperties(final Map<?, ?> properties) {
        if (properties != null)
            properties.isEmpty();
    }

    /**
     * Returns the list of required property names.
     *
     * @return the required property names
     */
    public List<String> getRequired() {
        return required;
    }

    /**
     * Sets the list of required property names.
     *
     * @param required the required property names
     */
    public void setRequired(List<String> required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSchema that = (JsonSchema) o;
        return Objects.equals(get$id(), that.get$id()) && Objects.equals(get$schema(), that.get$schema()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getType(), that.getType()) && Objects.equals(getProperties(), that.getProperties()) && Objects.equals(getRequired(), that.getRequired());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get$id(), get$schema(), getTitle(), getDescription(), getType(), getProperties(), getRequired());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonSchema{");
        sb.append("$id='").append($id).append('\'');
        sb.append(", $schema='").append($schema).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", properties=").append(properties);
        sb.append(", required=").append(required);
        sb.append('}');
        return sb.toString();
    }

}
