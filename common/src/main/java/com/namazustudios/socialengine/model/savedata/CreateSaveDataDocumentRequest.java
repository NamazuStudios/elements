package com.namazustudios.socialengine.model.savedata;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel(description = "Used to create a save data document on the remote.")
public class CreateSaveDataDocumentRequest {

    @NotNull
    @ApiModelProperty("The slot of the property. Must be unique for user or profile.")
    private int slot;

    @ApiModelProperty(
        "The id of the user which owns the save data. If specified, the user will own this save data and the " +
        "profileId must be null or be owned by the user specified.")
    private String userId;

    @ApiModelProperty(
        "The id of the profile which owns the save data. If specified, the profile will own this save data and " +
        "the userId must be null or be owned by the user specified.")
    private String profileId;

    @NotNull
    @ApiModelProperty("The contents of the save data.")
    private String contents;

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateSaveDataDocumentRequest that = (CreateSaveDataDocumentRequest) o;
        return getSlot() == that.getSlot() && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getContents(), that.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSlot(), getUserId(), getProfileId(), getContents());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateSaveDataDocumentRequest{");
        sb.append("slot=").append(slot);
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", profileId='").append(profileId).append('\'');
        sb.append(", contents='").append(contents).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
