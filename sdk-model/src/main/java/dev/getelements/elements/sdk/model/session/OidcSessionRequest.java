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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OidcSessionRequest that = (OidcSessionRequest) o;
        return Objects.equals(getJwt(), that.getJwt()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getProfileSelector(), that.getProfileSelector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJwt(), getProfileId(), getProfileSelector());
    }

    @Override
    public String toString() {
        return "OidcSessionRequest{" +
                "jwt='" + jwt + '\'' +
                ", profileId='" + profileId + '\'' +
                ", profileSelector='" + profileSelector + '\'' +
                '}';
    }

}
