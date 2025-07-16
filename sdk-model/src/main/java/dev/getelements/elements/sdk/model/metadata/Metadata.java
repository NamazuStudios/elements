package dev.getelements.elements.sdk.model.metadata;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

import java.util.Map;
import java.util.Objects;

public class Metadata {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = {ValidationGroups.Update.class, ValidationGroups.Read.class})
    @Schema(description = "The metadata object's database assigned unique ID.")
    private String id;

    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    @Schema(description = "A unique name for the metadata object.")
    private String name;

    @Schema(description = "An object containing the metadata payload as key-value pairs.")
    private Map<String, Object> metadata;

    @Schema(description = "The corresponding spec that defines the structure of the metadata.")
    private MetadataSpec spec;

    @Schema(description = "The minimum level of access required to view this metadata.")
    private User.Level accessLevel;

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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public MetadataSpec getSpec() {
        return spec;
    }

    public void setSpec(MetadataSpec spec) {
        this.spec = spec;
    }

    public User.Level getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(User.Level accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata that = (Metadata) o;
        return Objects.equals(id, that.getId()) && Objects.equals(name, that.getName()) && Objects.equals(metadata, that.getMetadata()) && Objects.equals(spec, that.getSpec()) && Objects.equals(accessLevel, that.getAccessLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, metadata, spec, accessLevel);
    }

    @Override
    public String toString() {
        return "CreateProfileRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", metadata='" + metadata + '\'' +
                ", spec='" + spec + '\'' +
                ", accessLevel=" + accessLevel +
                '}';
    }
}
