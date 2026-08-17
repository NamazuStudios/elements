package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/** Request to create a session using an OIDC (OpenID Connect) JWT token. */
public class OidcSessionRequest {

    /** Creates a new instance. */
    public OidcSessionRequest() {}

    @NotBlank
    @Schema(description = "The JWT to parse")
    private String jwt;

    @Schema(description = "The profile ID to assign to the session.")
    private String profileId;

    @Schema(description = "A query string to select the profile to use. " +
            "NOTE: This will not be run if a profileId is specified.")
    private String profileSelector;

    @Schema(description = "The name or ID of an application whose primary profile should be attached to the " +
            "session, auto-creating it (subject to the application's autoCreateProfile/maxProfiles settings) if " +
            "it does not exist. Only used if profileId and profileSelector are not specified. If unspecified, " +
            "the application encoded in the JWT's own claims (if any) is used instead, without auto-create.")
    private String applicationNameOrId;

    /**
     * Returns the JWT to parse.
     *
     * @return the JWT
     */
    public String getJwt() {
        return jwt;
    }

    /**
     * Sets the JWT to parse.
     *
     * @param jwt the JWT
     */
    public void setJwt(String jwt) {
        this.jwt = jwt;
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
     * Returns the profile selector query string.
     *
     * @return the profile selector
     */
    public String getProfileSelector() {
        return profileSelector;
    }

    /**
     * Sets the profile selector query string.
     *
     * @param profileSelector the profile selector
     */
    public void setProfileSelector(String profileSelector) {
        this.profileSelector = profileSelector;
    }

    /**
     * Returns the name or ID of the application whose primary profile should be attached to the session.
     *
     * @return the application name or ID
     */
    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    /**
     * Sets the name or ID of the application whose primary profile should be attached to the session.
     *
     * @param applicationNameOrId the application name or ID
     */
    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OidcSessionRequest that = (OidcSessionRequest) o;
        return Objects.equals(getJwt(), that.getJwt()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getProfileSelector(), that.getProfileSelector()) && Objects.equals(getApplicationNameOrId(), that.getApplicationNameOrId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJwt(), getProfileId(), getProfileSelector(), getApplicationNameOrId());
    }

    @Override
    public String toString() {
        return "OidcSessionRequest{" +
                "jwt='" + jwt + '\'' +
                ", profileId='" + profileId + '\'' +
                ", profileSelector='" + profileSelector + '\'' +
                ", applicationNameOrId='" + applicationNameOrId + '\'' +
                '}';
    }

}
