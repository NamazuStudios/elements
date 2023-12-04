package dev.getelements.elements.model.schema.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApiModel(description = "A Subset of the JSON-Schema - https://json-schema.org/draft/2020-12/json-schema-core")
public class JsonSchema {

    public static final String SCHEMA_DRAFT_2020_12 = "https://json-schema.org/draft/2020-12/schema";

    @ApiModelProperty("See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String $id;

    @ApiModelProperty("See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String $schema = SCHEMA_DRAFT_2020_12;

    @ApiModelProperty("See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String title;

    @ApiModelProperty("See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String description;

    @ApiModelProperty("See - https://json-schema.org/draft/2020-12/json-schema-core")
    private String type;

    @ApiModelProperty("See - https://json-schema.org/draft/2020-12/json-schema-core")
    private Map<String, JsonSchemaProperty> properties;

    @ApiModelProperty("See - https://json-schema.org/draft/2020-12/json-schema-core")
    private List<String> required;

    public String get$id() {
        return $id;
    }

    public void set$id(String $id) {
        this.$id = $id;
    }

    public String get$schema() {
        return $schema;
    }

    public void set$schema(String $schema) {
        this.$schema = $schema;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, JsonSchemaProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, JsonSchemaProperty> properties) {
        this.properties = properties;
    }

    public void fromProperties(final Map<?, ?> properties) {
        if (properties != null)
            properties.isEmpty();
    }

    public List<String> getRequired() {
        return required;
    }

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
