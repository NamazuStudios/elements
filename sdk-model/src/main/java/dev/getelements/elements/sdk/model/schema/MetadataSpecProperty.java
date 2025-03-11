package dev.getelements.elements.sdk.model.schema;



import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.WORD_ONLY;

@ValidProperties
public class MetadataSpecProperty implements Serializable, MetadataSpecPropertiesContainer {

    @NotNull
    @Schema(description = "The unique name of the field")
    @Pattern(regexp = WORD_ONLY)
    private String name;

    @NotNull
    @Schema(description = "The display name of the field")
    private String displayName;

    @NotNull
    @Schema(description = "The field type")
    private MetadataSpecPropertyType type;

    @Schema(description = "True if the field is required.")
    private boolean required;

    @Schema(description = "The placeholder description when displaying in the editor.")
    private String placeholder;

    @Schema(description = "The default description, null if left unspecified.")
    private Object defaultValue;

    @Valid
    private List<MetadataSpecProperty> properties;

    public MetadataSpecProperty() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetadataSpecPropertyType getType() {
        return type;
    }

    public void setType(MetadataSpecPropertyType type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public List<MetadataSpecProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MetadataSpecProperty> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataSpecProperty that = (MetadataSpecProperty) o;
        return isRequired() == that.isRequired() && Objects.equals(getName(), that.getName()) && Objects.equals(getDisplayName(), that.getDisplayName()) && getType() == that.getType() && Objects.equals(getPlaceholder(), that.getPlaceholder()) && Objects.equals(getDefaultValue(), that.getDefaultValue()) && Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDisplayName(), getType(), isRequired(), getPlaceholder(), getDefaultValue(), getProperties());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetadataSpecProperty{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", fieldType=").append(type);
        sb.append(", required=").append(required);
        sb.append(", placeHolder='").append(placeholder).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }

}
