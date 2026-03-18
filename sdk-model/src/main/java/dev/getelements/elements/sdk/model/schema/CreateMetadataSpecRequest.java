package dev.getelements.elements.sdk.model.schema;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/** Represents a request to create a MetadataSpec definition. */
@ValidProperties
@Schema(description = "Represents a request to create a MetadataSpec definition.")
public class CreateMetadataSpecRequest implements Serializable, MetadataSpecPropertiesContainer {

    /** Creates a new instance. */
    public CreateMetadataSpecRequest() {}

    @Schema(description = "The name of the metadata spec.")
    private String name;

    @Schema(description = "The type of the metadata spec.")
    private MetadataSpecPropertyType type;

    @Valid
    @NotNull
    @Schema(description = "The token template tabs to create.")
    private List<MetadataSpecProperty> properties;

    /**
     * Returns the name of the metadata spec.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the metadata spec.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    /**
     * Returns the type of the metadata spec.
     *
     * @return the type
     */
    public MetadataSpecPropertyType getType() {
        return type;
    }

    /**
     * Sets the type of the metadata spec.
     *
     * @param type the type
     */
    public void setType(MetadataSpecPropertyType type) {
        this.type = type;
    }

    @Override
    /**
     * Returns the properties of the metadata spec.
     *
     * @return the properties
     */
    public List<MetadataSpecProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the properties of the metadata spec.
     *
     * @param properties the properties
     */
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
