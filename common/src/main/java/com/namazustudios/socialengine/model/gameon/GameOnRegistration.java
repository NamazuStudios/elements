package com.namazustudios.socialengine.model.gameon;

import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Associates an Amazon GameOn registration with a particular profile.  There may exist only " +
                        "one GameOnRegistration per Profile at a time.")
public class GameOnRegistration implements Serializable {

    @ApiModelProperty("The unique ID of this registration.")
    private String id;

    @ApiModelProperty("The profile assocaited with this GameOn registration.")
    private Profile profile;

    @ApiModelProperty("The Amazon-issued Player Token")
    private String playerToken;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnRegistration)) return false;
        GameOnRegistration that = (GameOnRegistration) object;
        return Objects.equals(getId(), that.getId()) &&
               Objects.equals(getProfile(), that.getProfile()) &&
               Objects.equals(getPlayerToken(), that.getPlayerToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProfile(), getPlayerToken());
    }

}
