package dev.getelements.elements.sdk.model.metadata;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

/** Represents a request to create a metadata object. */
@Schema
public class CreateMetadataRequest {

    /** Creates a new instance. */
    public CreateMetadataRequest() {}

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    @Schema(description = "A unique name for the metadata object.")
    private String name;

    @Schema(description = "An object containing the metadata payload as key-value pairs.")
    private Map<String, Object> metadata;

    @Schema(description = "The corresponding spec that defines the structure of the metadata.")
    private MetadataSpec metadataSpec;

    @Schema(description = "The minimum level of access required to view this metadata.")
    private User.Level accessLevel;

    /**
     * Returns the unique name for the metadata object.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name for the metadata object.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the metadata payload.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata payload.
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
     * Returns the minimum access level required to view this metadata.
     *
     * @return the access level
     */
    public User.Level getAccessLevel() {
        return accessLevel;
    }

    /**
     * Sets the minimum access level required to view this metadata.
     *
     * @param accessLevel the access level
     */
    public void setAccessLevel(User.Level accessLevel) {
        this.accessLevel = accessLevel;
    }
}
