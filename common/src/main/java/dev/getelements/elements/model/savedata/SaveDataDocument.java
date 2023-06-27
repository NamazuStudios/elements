package dev.getelements.elements.model.savedata;

import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Update;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.util.Hex;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import java.util.Objects;

@ApiModel(description = "Represents an arbitrary save data document that persists on the server. The document " +
                        "saves with a version identifier such that clients may resolve conflicts between the local " +
                        "copy and the remote copy.")
public class SaveDataDocument {

    @Null(groups = Insert.class)
    @ApiModelProperty("The database assigned unique ID of the document.")
    private String id;

    @Min(0) @Max(Integer.MAX_VALUE)
    @ApiModelProperty("The slot of the property.")
    private int slot;

    @ApiModelProperty("The user which owns the save data.")
    private User user;

    @ApiModelProperty("The profile which owns the save data.")
    private Profile profile;

    @Min(0) @Max(Long.MAX_VALUE)
    @ApiModelProperty("The timestamp of the last write to this document.")
    private long timestamp;

    @Null(groups = Insert.class)
    @NotNull(groups = Update.class)
    @Pattern(regexp = Hex.VALID_REGEX)
    @ApiModelProperty("The revision of the save data document.")
    private String version;

    @NotNull
    @ApiModelProperty("The contents of the save data.")
    private String contents;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
        SaveDataDocument that = (SaveDataDocument) o;
        return getSlot() == that.getSlot() && getTimestamp() == that.getTimestamp() && Objects.equals(getId(), that.getId()) && Objects.equals(user, that.user) && Objects.equals(getProfile(), that.getProfile()) && Objects.equals(getVersion(), that.getVersion()) && Objects.equals(getContents(), that.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSlot(), user, getProfile(), getTimestamp(), getVersion(), getContents());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SaveDataDocument{");
        sb.append("id='").append(id).append('\'');
        sb.append(", slot=").append(slot);
        sb.append(", user=").append(user);
        sb.append(", profile=").append(profile);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", version='").append(version).append('\'');
        sb.append(", contents='").append(contents).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
