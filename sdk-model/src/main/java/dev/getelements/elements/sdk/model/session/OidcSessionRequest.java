package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public class OidcSessionRequest {

    @NotBlank
    @Schema(description = "The JWT to parse")
    private String jwt;

    @Schema(description = "The profile ID to assign to the session.")
    private String profileId;

    @Schema(description = "A query string to select the profile to use. " +
            "NOTE: This will not be run if a profileId is specified.")
    private String profileSelector;

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileSelector() {
        return profileSelector;
    }

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
