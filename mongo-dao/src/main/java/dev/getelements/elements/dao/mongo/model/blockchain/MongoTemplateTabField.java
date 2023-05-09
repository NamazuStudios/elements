package dev.getelements.elements.dao.mongo.model.blockchain;

import dev.getelements.elements.BlockchainConstants;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import io.swagger.annotations.ApiModelProperty;

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
    private BlockchainConstants.TemplateFieldType fieldType = BlockchainConstants.TemplateFieldType.Enum;


    public MongoTemplateTabField() {
    }

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

    public void setFieldType(BlockchainConstants.TemplateFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public BlockchainConstants.TemplateFieldType getFieldType() {
        return fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoTemplateTabField)) return false;
        MongoTemplateTabField contract = (MongoTemplateTabField) o;
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
