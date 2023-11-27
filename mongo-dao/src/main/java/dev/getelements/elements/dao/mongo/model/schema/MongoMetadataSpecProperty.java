package dev.getelements.elements.dao.mongo.model.schema;

import dev.getelements.elements.model.schema.MetadataSpecPropertyType;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.List;
import java.util.Objects;

@Embedded
public class MongoMetadataSpecProperty {

    @Property
    private String name;

    @Property
    private String displayName;

    @Property
    private boolean required;

    @Property
    private String placeholder;

    @Property
    private String defaultValue;

    @Property
    private MetadataSpecPropertyType type;

    @Property
    private List<MongoMetadataSpecProperty> properties;

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

    public MetadataSpecPropertyType getType() {
        return type;
    }

    public void setType(MetadataSpecPropertyType type) {
        this.type = type;
    }

    public List<MongoMetadataSpecProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MongoMetadataSpecProperty> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoMetadataSpecProperty that = (MongoMetadataSpecProperty) o;
        return isRequired() == that.isRequired() && Objects.equals(getName(), that.getName()) && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getPlaceholder(), that.getPlaceholder()) && Objects.equals(getDefaultValue(), that.getDefaultValue()) && getType() == that.getType() && Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDisplayName(), isRequired(), getPlaceholder(), getDefaultValue(), getType(), getProperties());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoMetadataSpecProperty{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", required=").append(required);
        sb.append(", placeholder='").append(placeholder).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", type=").append(type);
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }

}
