package dev.getelements.elements.model.schema;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.Constants.Regexp.WHOLE_WORD_ONLY;
import static java.lang.String.format;

@ValidProperties
public class MetadataSpecProperty implements Serializable, MetadataSpecPropertiesContainer {

    @NotNull
    @ApiModelProperty("The unique name of the field")
    @Pattern(regexp = WHOLE_WORD_ONLY)
    private String name;

    @NotNull
    @ApiModelProperty("The display name of the field")
    private String displayName;

    @NotNull
    @ApiModelProperty("The field type")
    private MetadataSpecPropertyType type;

    @NotNull
    @ApiModelProperty("True if the field is required.")
    private boolean required;

    @ApiModelProperty("The placeholder value when displaying in the editor.")
    private String placeholder;

    @ApiModelProperty("The default value, if left unspecified.")
    private String defaultValue;

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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
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
