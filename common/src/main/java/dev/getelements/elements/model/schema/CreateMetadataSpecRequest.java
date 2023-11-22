package dev.getelements.elements.model.schema;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@ValidProperties
@ApiModel(description = "Represents a request to create a MetadataSpec definition.")
public class CreateMetadataSpecRequest implements Serializable, MetadataSpecPropertiesContainer {

    @ApiModelProperty("The name of the metadata spec.")
    private String name;

    @ApiModelProperty("The type of the metadata spec.")
    private MetadataSpecPropertyType type;

    @Valid
    @NotNull
    @ApiModelProperty("The token template tabs to create.")
    private List<MetadataSpecProperty> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
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
        CreateMetadataSpecRequest that = (CreateMetadataSpecRequest) o;
        return Objects.equals(getProperties(), that.getProperties()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProperties(), getName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateMetadataSpecRequest{");
        sb.append("properties=").append(properties);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
