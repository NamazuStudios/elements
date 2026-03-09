package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/** Represents a request to create a session using a username and password. */
@Schema
public class UsernamePasswordSessionRequest {

    /** Creates a new instance. */
    public UsernamePasswordSessionRequest() {}

    @NotBlank
    @Schema(description = "The user ID.")
    private String userId;

    @NotBlank
    @Schema(description = "The password.")
    private String password;

    @Schema(description = "The profile ID to assign to the session.")
    private String profileId;

    @Schema(description = "A query string to select the profile to use.")
    private String profileSelector;

    /**
     * Returns the user ID (login name or email) for this session request.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID (login name or email) for this session request.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the plaintext password for this session request.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the plaintext password for this session request.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the profile ID to assign to the session.
     *
     * @return the profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profile ID to assign to the session.
     *
     * @param profileId the profile ID
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * Returns a query string used to select the profile for this session.
     *
     * @return the profile selector query string
     */
    public String getProfileSelector() {
        return profileSelector;
    }

    /**
     * Sets a query string used to select the profile for this session.
     *
     * @param profileSelector the profile selector query string
     */
    public void setProfileSelector(String profileSelector) {
        this.profileSelector = profileSelector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsernamePasswordSessionRequest that = (UsernamePasswordSessionRequest) o;
        return Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getProfileSelector(), that.getProfileSelector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getPassword(), getProfileId(), getProfileSelector());
    }

    @Override
    public String toString() {
        return "UsernamePasswordSessionRequest{" +
                "userId='" + userId + '\'' +
                ", password='...you keep your secrets" + '\'' +
                ", profileId='" + profileId + '\'' +
                ", profileSelector='" + profileSelector + '\'' +
                '}';
    }

}
