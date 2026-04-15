package dev.getelements.elements.sdk.model.user;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

/** Represents a unique identifier that links a user to an external authentication scheme. */
@Schema
public class UserUid implements Serializable {

    public static final String USER_UID_CREATED_EVENT = "dev.getelements.user_uid.created";
    public static final String USER_UID_UPDATED_EVENT = "dev.getelements.user_uid.updated";
    public static final String USER_UID_DELETED_EVENT = "dev.getelements.user_uid.deleted";

    /** Creates a new instance. */
    public UserUid() {}

    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The scheme id mapped associated with the OID value. Schemes prepended with dev.getelements are reserved.")
    private String scheme;

    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The id associated with this scheme.")
    private String id;

    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The id of the user associated with this User UID.")
    private String userId;

    @Schema(description = "The verification status of this UID.")
    private VerificationStatus verificationStatus;

    /**
     * Returns the user ID associated with this UID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID associated with this UID.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the authentication scheme name associated with this UID.
     *
     * @return the scheme name
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the authentication scheme name associated with this UID.
     *
     * @param scheme the scheme name
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Returns the external identifier value within the scheme.
     *
     * @return the external ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the external identifier value within the scheme.
     *
     * @param id the external ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the verification status of this UID.
     *
     * @return the verification status, or {@link VerificationStatus#UNVERIFIED} if not set
     */
    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    /**
     * Sets the verification status of this UID.
     *
     * @param verificationStatus the verification status
     */
    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
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
