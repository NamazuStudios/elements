package dev.getelements.elements.sdk.model.invite;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;
import java.util.Objects;

@Schema(description = "Represents a request to invite users with phone numbers list")
public class InviteViaPhonesRequest {

    @Schema(description = "The list of phone numbers")
    private List<String> phoneNumbers;

    //TODO: optional query

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InviteViaPhonesRequest that = (InviteViaPhonesRequest) o;
        return Objects.equals(phoneNumbers, that.phoneNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumbers);
    }

    @Override
    public String toString() {
        return "InviteViaPhonesRequest{" +
                "phoneNumbers=" + phoneNumbers +
                '}';
    }

}
