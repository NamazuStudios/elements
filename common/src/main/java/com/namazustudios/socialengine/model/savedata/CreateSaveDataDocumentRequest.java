package com.namazustudios.socialengine.model.savedata;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel(description = "Used to create a save data document on the remote.")
public class CreateSaveDataDocumentRequest {

    @NotNull
    @ApiModelProperty("The slot of the property. Must be unique")
    private int slot;

    @ApiModelProperty("The id of the profile which owns the save data.")
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
        return getSlot() == that.getSlot() && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getContents(), that.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSlot(), getProfileId(), getContents());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateSaveDataDocumentRequest{");
        sb.append("slot=").append(slot);
        sb.append(", profileId='").append(profileId).append('\'');
        sb.append(", contents='").append(contents).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
