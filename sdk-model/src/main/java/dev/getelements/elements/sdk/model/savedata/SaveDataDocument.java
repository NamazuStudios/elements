package dev.getelements.elements.sdk.model.savedata;

import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.*;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.HEX_VALID_REGEX;

@Schema(description = "Represents an arbitrary save data document that persists on the server. The document " +
                        "saves with a version identifier such that clients may resolve conflicts between the local " +
                        "copy and the remote copy.")
public class SaveDataDocument {

    @Null(groups = Insert.class)
    @Schema(description = "The database assigned unique ID of the document.")
    private String id;

    @Min(0) @Max(Integer.MAX_VALUE)
    @Schema(description = "The slot of the property.")
    private int slot;

    @Schema(description = "The user which owns the save data.")
    private User user;

    @Schema(description = "The profile which owns the save data.")
    private Profile profile;

    @Min(0) @Max(Long.MAX_VALUE)
    @Schema(description = "The timestamp of the last write to this document.")
    private long timestamp;

    @Null(groups = Insert.class)
    @NotNull(groups = Update.class)
    @Pattern(regexp = HEX_VALID_REGEX)
    @Schema(description = "The revision of the save data document.")
    private String version;

    @NotNull
    @Schema(description = "The contents of the save data.")
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
