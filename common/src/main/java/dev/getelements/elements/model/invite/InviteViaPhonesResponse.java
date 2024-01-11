package dev.getelements.elements.model.invite;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description = "Represents a response to invite users with phone numbers list")
public class InviteViaPhonesResponse {

    @ApiModelProperty("The list of objects representing matched phone numbers")
    private List<PhoneMatchedInvitation> matched;

    public List<PhoneMatchedInvitation> getMatched() {
        return matched;
    }

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
