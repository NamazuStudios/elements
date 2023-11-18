package dev.getelements.elements.dao.mongo.model.schema;

import dev.getelements.elements.model.schema.template.TemplateFieldType;
import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@Embedded
public class MongoTemplateTabField {

    @Property
    private String name;

    @Property
    private String displayName;

    @ApiModelProperty("isRequired")
    private Boolean isRequired;

    @ApiModelProperty("placeHolder")
    private String placeHolder;

    @ApiModelProperty("defaultValue")
    private String defaultValue;

    @Property
    private TemplateFieldType fieldType = TemplateFieldType.ENUM;

    @Property
    private List<TemplateTab> tabs;

    public MongoTemplateTabField() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setRequired(Boolean required) {
        isRequired = required;
    }

    public void setIsRequired(Boolean required) {
        isRequired = required;
    }

    public Boolean getIsRequired() {
        return isRequired;
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

    public void setFieldType(TemplateFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public TemplateFieldType getFieldType() {
        return fieldType;
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
        MongoTemplateTabField that = (MongoTemplateTabField) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getIsRequired(), that.getIsRequired()) && Objects.equals(getPlaceHolder(), that.getPlaceHolder()) && Objects.equals(getDefaultValue(), that.getDefaultValue()) && getFieldType() == that.getFieldType() && Objects.equals(tabs, that.tabs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDisplayName(), getIsRequired(), getPlaceHolder(), getDefaultValue(), getFieldType(), tabs);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoTemplateTabField{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", isRequired=").append(isRequired);
        sb.append(", placeHolder='").append(placeHolder).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", fieldType=").append(fieldType);
        sb.append(", tabs=").append(tabs);
        sb.append('}');
        return sb.toString();
    }

}
