package dev.getelements.elements.model.schema.template;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class TemplateTabField {

    @ApiModelProperty("name")
    private String name;

    @ApiModelProperty("displayName")
    private String displayName;

    @ApiModelProperty("fieldType")
    private TemplateFieldType fieldType = TemplateFieldType.Enum;

    @ApiModelProperty("isRequired")
    private Boolean isRequired;

    @ApiModelProperty("placeHolder")
    private String placeHolder;

    @ApiModelProperty("defaultValue")
    private String defaultValue;


    public TemplateTabField() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateTabField)) return false;
        TemplateTabField contract = (TemplateTabField) o;
        return Objects.equals(getName(), contract.getName()) &&
                Objects.equals(getFieldType(), contract.getFieldType()) &&
                Objects.equals(getDisplayName(), contract.getDisplayName()) &&
                        Objects.equals(getRequired(), contract.getRequired()) &&
                                Objects.equals(getDefaultValue(), contract.getDefaultValue()) &&
                                        Objects.equals(getPlaceHolder(), contract.getPlaceHolder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getFieldType(), getDisplayName(), getRequired(), getDefaultValue(), getPlaceHolder());
    }

    @Override
    public String toString() {
        return "TemplateTabField{" +
                "name='" + name + '\'' +
                "displayName='" + displayName + '\'' +
                ", isRequired=" + isRequired +
                ", defaultValue=" + defaultValue +
                ", placeHolder=" + placeHolder +
                '}';
    }
}
