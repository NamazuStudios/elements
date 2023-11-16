package dev.getelements.elements.model.schema.template;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TemplateTabField {

    @NotNull
    @ApiModelProperty("name")
    private String name;

    @NotNull
    @ApiModelProperty("displayName")
    private String displayName;

    @NotNull
    @ApiModelProperty("fieldType")
    private TemplateFieldType fieldType;

    @NotNull
    @ApiModelProperty("isRequired")
    private Boolean isRequired;

    @NotNull
    @ApiModelProperty("placeHolder")
    private String placeHolder;

    @NotNull
    @ApiModelProperty("defaultValue")
    private String defaultValue;

    @NotNull
    @ApiModelProperty("tabs")
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
