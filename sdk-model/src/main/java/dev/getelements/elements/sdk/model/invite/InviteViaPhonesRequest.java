package dev.getelements.elements.sdk.model.invite;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;
import java.util.Objects;

/** Represents a request to invite users via a list of phone numbers. */
@Schema(description = "Represents a request to invite users with phone numbers list")
public class InviteViaPhonesRequest {

    /** Creates a new instance. */
    public InviteViaPhonesRequest() {}

    @Schema(description = "The list of phone numbers")
    private List<String> phoneNumbers;

    //TODO: optional query

    /**
     * Returns the list of phone numbers.
     *
     * @return the phone numbers
     */
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Sets the list of phone numbers.
     *
     * @param phoneNumbers the phone numbers
     */
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
