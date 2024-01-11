package dev.getelements.elements.model.invite;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description =
        "Represents a single match between the user primary phone number and requested phone from invitation.")
public class PhoneMatchedInvitation {

    @ApiModelProperty("Phone number")
    private String phoneNumber;

    @ApiModelProperty("The list of profile Ids that phone was matched")
    private List<String> profileIds;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getProfileIds() {
        return profileIds;
    }

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
