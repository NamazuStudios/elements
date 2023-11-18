package dev.getelements.elements.model.schema.template;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.getelements.elements.Constants.Regexp.WHOLE_WORD_ONLY;

public class TemplateTabField implements Serializable {

    @NotNull
    @ApiModelProperty("The unique name of the field")
    @Pattern(regexp = WHOLE_WORD_ONLY)
    private String name;

    @NotNull
    @ApiModelProperty("The display name of the field")
    private String displayName;

    @NotNull
    @ApiModelProperty("The field type")
    private TemplateFieldType fieldType;

    @NotNull
    @ApiModelProperty("True if the field is required.")
    private Boolean isRequired;

    @ApiModelProperty("The placeholder value when displaying in the editor.")
    private String placeHolder;

    @ApiModelProperty("The default value, if left unspecified.")
    private String defaultValue;

    @Valid
    @ApiModelProperty("The list of tabs (only applicable if this is of OBJECT type).")
    private List<TemplateTab> tabs;

    public TemplateTabField() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TemplateFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(TemplateFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean required) {
        isRequired = required;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setRequired(Boolean required) {
        isRequired = required;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<TemplateTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateTabField that = (TemplateTabField) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getDisplayName(), that.getDisplayName()) && getFieldType() == that.getFieldType() && Objects.equals(getIsRequired(), that.getIsRequired()) && Objects.equals(getPlaceHolder(), that.getPlaceHolder()) && Objects.equals(getDefaultValue(), that.getDefaultValue()) && Objects.equals(getTabs(), that.getTabs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDisplayName(), getFieldType(), getIsRequired(), getPlaceHolder(), getDefaultValue(), getTabs());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TemplateTabField{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", fieldType=").append(fieldType);
        sb.append(", isRequired=").append(isRequired);
        sb.append(", placeHolder='").append(placeHolder).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", tabs=").append(tabs);
        sb.append('}');
        return sb.toString();
    }

}
