package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Associates an Amazon GameOn registration with a particular profile.  There may exist only " +
                        "one GameOnRegistration per Profile at a time.")
public class GameOnRegistration implements Serializable {

    @Null(groups = {Create.class, Insert.class})
    @ApiModelProperty("The unique ID of this registration.")
    private String id;

    @NotNull(groups = Insert.class)
    @ApiModelProperty("The profile assocaited with this GameOn registration.")
    private Profile profile;

    @Null(groups = Create.class, message = "Must not specify player token when creating.")
    @NotNull(groups = Insert.class, message = "Must be defined for insert operations.")
    @ApiModelProperty("The Amazon-issued Player Token")
    private String playerToken;

    @Null(groups = Create.class, message = "Must not specify external player id when creating.")
    @NotNull(groups = Insert.class, message = "Must be defined for insert operations.")
    @ApiModelProperty("The Amazon-issued external player ID")
    private String externalPlayerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String playerToken) {
        this.playerToken = playerToken;
    }

    public String getExternalPlayerId() {
        return externalPlayerId;
    }

    public void setExternalPlayerId(String externalPlayerId) {
        this.externalPlayerId = externalPlayerId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnRegistration)) return false;
        GameOnRegistration that = (GameOnRegistration) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getProfile(), that.getProfile()) &&
                Objects.equals(getPlayerToken(), that.getPlayerToken()) &&
                Objects.equals(getExternalPlayerId(), that.getExternalPlayerId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProfile(), getPlayerToken(), getExternalPlayerId());
    }

    @Override
    public String toString() {
        return "GameOnRegistration{" +
                "id='" + id + '\'' +
                ", profile=" + profile +
                ", playerToken='" + playerToken + '\'' +
                ", externalPlayerId='" + externalPlayerId + '\'' +
                '}';
    }

}
