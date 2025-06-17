package dev.getelements.elements.sdk.model.schema;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

//TODO: whole model should be SU visible
@ValidProperties
@Schema(description = "Represents a spec for metadata.")
public class MetadataSpec implements Serializable, MetadataSpecPropertiesContainer {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the schema itself.")
    private String id;

    @NotNull
    @Schema(description = "The Name of the schema.")
    private String name;

    @NotNull
    @Schema(description = "The type of the tab itself.")
    private MetadataSpecPropertyType type;

    @Valid
    @NotNull
    @Schema(description = "The tabs of the metadata spec.")
    private List<MetadataSpecProperty> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        MetadataSpec that = (MetadataSpec) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                getType() == that.getType() &&
                Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getType(), getProperties());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetadataSpec{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", type=").append(type);
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }

}
