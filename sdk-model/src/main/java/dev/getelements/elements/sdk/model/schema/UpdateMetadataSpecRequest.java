package dev.getelements.elements.sdk.model.schema;

import io.swagger.v3.oas.annotations.media.Schema;


import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/** Represents a request to update a MetadataSpec's name, type, and properties. */
@ValidProperties
@Schema(description = "Represents a request to update a MetadataSpec.")
public class UpdateMetadataSpecRequest implements Serializable, MetadataSpecPropertiesContainer {

    /** Creates a new instance. */
    public UpdateMetadataSpecRequest() {}

    @Schema(description = "The name of the metadata spec.")
    private String name;

    @Schema(description = "The type of the metadata spec.")
    private MetadataSpecPropertyType type;

    @Schema(description = "The updated list of properties.")
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

    /**
     * Returns the type of the metadata spec.
     *
     * @return the type
     */
    @Override
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

    /**
     * Returns the updated list of properties for the metadata spec.
     *
     * @return the properties
     */
    @Override
    public List<MetadataSpecProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the updated list of properties for the metadata spec.
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
