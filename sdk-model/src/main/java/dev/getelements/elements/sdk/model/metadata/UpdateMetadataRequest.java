package dev.getelements.elements.sdk.model.metadata;

import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public class UpdateMetadataRequest {

    @Schema(description = "An object containing the metadata payload as key-value pairs.")
    private Map<String, Object> metadata;

    @Schema(description = "The corresponding spec that defines the structure of the metadata.")
    private MetadataSpec metadataSpec;

    @Schema(description = "The minimum level of access required to view this metadata.")
    private User.Level accessLevel;

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public MetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    public void setMetadataSpec(MetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

    public User.Level getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(User.Level accessLevel) {
        this.accessLevel = accessLevel;
    }
}
