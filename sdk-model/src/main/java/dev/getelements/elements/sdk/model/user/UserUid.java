package dev.getelements.elements.sdk.model.user;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Schema
public class UserUid implements Serializable {

    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The scheme id mapped associated with the OID value. Schemes prepended with dev.getelements are reserved.")
    private String scheme;

    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The id associated with this scheme.")
    private String id;

    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The id of the user associated with this User UID.")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, id, userId);
    }

    @Override
    public String toString() {
        return "UserUid{" +
                "scheme=" + scheme +
                ", id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
