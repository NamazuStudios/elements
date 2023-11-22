package dev.getelements.elements.model.schema;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Represents a request to update a MetadataSpec.")
public class UpdateMetadataSpecRequest {

    @ApiModelProperty("The name of the schema.")
    private String name;

    @NotNull
    @ApiModelProperty("The updated token template tabs.")
    List<MetadataSpecProperty> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
        UpdateMetadataSpecRequest that = (UpdateMetadataSpecRequest) o;
        return Objects.equals(getProperties(), that.getProperties()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProperties(), getName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateMetadataSpecRequest{");
        sb.append("properties=").append(properties);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
