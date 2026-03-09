package dev.getelements.elements.sdk.model.metadata;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/** Represents a custom metadata object. */
@Schema(description = "Represents a custom metadata object.")
public class Metadata implements Serializable {

    /** Creates a new instance. */
    public Metadata() {}

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = {ValidationGroups.Update.class, ValidationGroups.Read.class})
    @Schema(description = "The metadata object's database assigned unique ID.")
    private String id;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Read.class, ValidationGroups.Update.class})
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    @Schema(description = "A unique name for the metadata object.")
    private String name;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Update.class, ValidationGroups.Read.class})
    @Schema(description = "An object containing the metadata payload as key-value pairs.")
    private Map<String, Object> metadata;

    @Schema(description = "The corresponding spec that defines the structure of the metadata.")
    private MetadataSpec metadataSpec;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Update.class, ValidationGroups.Read.class})
    @Schema(description = "The minimum level of access required to view this metadata.")
    private User.Level accessLevel;

    /**
     * Returns the unique ID of the metadata object.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the metadata object.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique name of the metadata object.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the metadata object.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the metadata payload as key-value pairs.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata payload as key-value pairs.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the metadata spec.
     *
     * @return the metadata spec
     */
    public MetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    /**
     * Sets the metadata spec.
     *
     * @param metadataSpec the metadata spec
     */
    public void setMetadataSpec(MetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

    /**
     * Returns the access level required to view this metadata.
     *
     * @return the access level
     */
    public User.Level getAccessLevel() {
        return accessLevel;
    }

    /**
     * Sets the access level required to view this metadata.
     *
     * @param accessLevel the access level
     */
    public void setAccessLevel(User.Level accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata that = (Metadata) o;
        return Objects.equals(id, that.getId()) && Objects.equals(name, that.getName()) && Objects.equals(metadata, that.getMetadata()) && Objects.equals(metadataSpec, that.getMetadataSpec()) && Objects.equals(accessLevel, that.getAccessLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, metadata, metadataSpec, accessLevel);
    }

    @Override
    public String toString() {
        return "CreateProfileRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", metadata='" + metadata + '\'' +
                ", spec='" + metadataSpec + '\'' +
                ", accessLevel=" + accessLevel +
                '}';
    }
}
