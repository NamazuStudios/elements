package dev.getelements.elements.sdk.model.schema.json;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Represents a property definition within a JSON Schema. */
@Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
public class JsonSchemaProperty {

    /** Creates a new instance. */
    public JsonSchemaProperty() {}

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String type;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String title;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String description;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private Map<String, JsonSchemaProperty> properties;

    @Schema(description = "See - https://json-schema.org/draft/2020-12/json-schema-core")
    private List<String> required;

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
     * Returns the nested properties map.
     *
     * @return the properties
     */
    public Map<String, JsonSchemaProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the nested properties map.
     *
     * @param properties the properties
     */
    public void setProperties(Map<String, JsonSchemaProperty> properties) {
        this.properties = properties;
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
        JsonSchemaProperty that = (JsonSchemaProperty) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getProperties(), that.getProperties()) && Objects.equals(getRequired(), that.getRequired());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getTitle(), getDescription(), getProperties(), getRequired());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonSchemaProperty{");
        sb.append("type='").append(type).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", properties=").append(properties);
        sb.append(", required=").append(required);
        sb.append('}');
        return sb.toString();
    }

}
