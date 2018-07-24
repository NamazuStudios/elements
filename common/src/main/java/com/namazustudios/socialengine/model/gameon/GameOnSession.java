package com.namazustudios.socialengine.model.gameon;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;

@ApiModel(description = "Represents a GameOn session stored and managed by Elements.  This is designed to be a 1:1 " +
                        "overlap with the Amazon GameOn API with additional Elements add-ons.")
public class GameOnSession {

    @NotNull
    @ApiModelProperty("The Elements assigned session ID.")
    @Null(groups = {Insert.class, Create.class})
    private String id;

    @Null(groups = Create.class)
    @NotNull(groups = Insert.class)
    @ApiModelProperty("The Amazon GameOn assigned session ID.")
    private String sessionId;

    @Null(groups = Create.class)
    @NotNull(groups = Insert.class)
    @ApiModelProperty("The Amazon GameOn assigned API Key.")
    private String sessionApiKey;

    @Null(groups = Create.class)
    @NotNull(groups = Insert.class)
    @ApiModelProperty("The time at which the session expires.")
    private long sessionExpirationDate;

    @NotNull(groups = Insert.class)
    @ApiModelProperty("The profile that owns this particualr session.")
    private Profile profile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionApiKey() {
        return sessionApiKey;
    }

    public void setSessionApiKey(String sessionApiKey) {
        this.sessionApiKey = sessionApiKey;
    }

    public long getSessionExpirationDate() {
        return sessionExpirationDate;
    }

    public void setSessionExpirationDate(long sessionExpirationDate) {
        this.sessionExpirationDate = sessionExpirationDate;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnSession)) return false;
        GameOnSession that = (GameOnSession) object;
        return getSessionExpirationDate() == that.getSessionExpirationDate() &&
                Objects.equals(getSessionId(), that.getSessionId()) &&
                Objects.equals(getSessionApiKey(), that.getSessionApiKey()) &&
                Objects.equals(getProfile(), that.getProfile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSessionId(), getSessionApiKey(), getSessionExpirationDate(), getProfile());
    }

    @Override
    public String toString() {
        return "GameOnSession{" +
                "sessionId='" + sessionId + '\'' +
                ", sessionApiKey='" + sessionApiKey + '\'' +
                ", sessionExpirationDate=" + sessionExpirationDate +
                ", profile=" + profile +
                '}';
    }

}
