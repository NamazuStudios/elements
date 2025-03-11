package dev.getelements.elements.sdk.model.notification;

import dev.getelements.elements.sdk.model.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;

/**
 * Represents a notification registration with Firebase.  This associates a Firebase registration token with a
 * particular {@link Profile} in the database.
 */
@Schema(description = "Represents a Firebase Cloud Messaging Registration Token")
public class FCMRegistration {

    @Schema(description = "The the unique id of the token stored in the database.")
    private String id;

    @NotNull
    @Schema(description = "The actual Firebase registration.")
    private String registrationToken;

    @Schema(description = "The Profile associated with this registration.")
    private Profile profile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FCMRegistration)) return false;

        FCMRegistration that = (FCMRegistration) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getRegistrationToken() != null ? !getRegistrationToken().equals(that.getRegistrationToken()) : that.getRegistrationToken() != null)
            return false;
        return getProfile() != null ? getProfile().equals(that.getProfile()) : that.getProfile() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getRegistrationToken() != null ? getRegistrationToken().hashCode() : 0);
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        return result;
    }

}
