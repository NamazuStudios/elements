package dev.getelements.elements.sdk.model.invite;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

/** Represents a response to an invite request via a list of phone numbers. */
@Schema(description = "Represents a response to invite users with phone numbers list")
public class InviteViaPhonesResponse {

    /** Creates a new instance. */
    public InviteViaPhonesResponse() {}

    @Schema(description = "The list of objects representing matched phone numbers")
    private List<PhoneMatchedInvitation> matched;

    /**
     * Returns the list of matched phone invitations.
     *
     * @return the matched invitations
     */
    public List<PhoneMatchedInvitation> getMatched() {
        return matched;
    }

    /**
     * Sets the list of matched phone invitations.
     *
     * @param matched the matched invitations
     */
    public void setMatched(List<PhoneMatchedInvitation> matched) {
        this.matched = matched;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InviteViaPhonesResponse that = (InviteViaPhonesResponse) o;
        return Objects.equals(matched, that.matched);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matched);
    }

    @Override
    public String toString() {
        return "InviteViaPhonesResponse{" +
                "matched=" + matched +
                '}';
    }
}
