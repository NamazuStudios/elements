package dev.getelements.elements.sdk.model.invite;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;
import java.util.Objects;

/** Represents a single match between the user's primary phone number and a requested phone number from an invitation. */
@Schema(description = "Represents a single match between the user primary phone number and requested phone from invitation.")
public class PhoneMatchedInvitation {

    /** Creates a new instance. */
    public PhoneMatchedInvitation() {}

    @Schema(description = "Phone number")
    private String phoneNumber;

    @Schema(description = "The list of profile Ids that phone was matched")
    private List<String> profileIds;

    /**
     * Returns the phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number.
     *
     * @param phoneNumber the phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the list of profile IDs that matched this phone number.
     *
     * @return the profile IDs
     */
    public List<String> getProfileIds() {
        return profileIds;
    }

    /**
     * Sets the list of profile IDs that matched this phone number.
     *
     * @param profileIds the profile IDs
     */
    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneMatchedInvitation that = (PhoneMatchedInvitation) o;
        return Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(profileIds, that.profileIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, profileIds);
    }

    @Override
    public String toString() {
        return "PhoneMatchedInvitation{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", profileIds=" + profileIds +
                '}';
    }

}
