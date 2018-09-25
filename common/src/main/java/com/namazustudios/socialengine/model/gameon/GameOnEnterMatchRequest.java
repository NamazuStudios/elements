package com.namazustudios.socialengine.model.gameon;

import com.namazustudios.socialengine.model.gameon.game.AppBuildType;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import com.namazustudios.socialengine.model.match.Match;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@ApiModel(description = "Defines the data necessary to enter a match.")
public class GameOnEnterMatchRequest implements Serializable {

    @ApiModelProperty("The device OS Type, used to create or reference the session.")
    private DeviceOSType deviceOSType;

    @ApiModelProperty("App build type, used to create or reference the session.")
    private AppBuildType appBuildType;

    @ApiModelProperty("The player attribues, if applicable.  This may be empty or null.")
    private Map<String, Object> playerAttributes;

    @ApiModelProperty("The Match")
    private Match match;

    public DeviceOSType getDeviceOSType() {
        return deviceOSType;
    }

    public void setDeviceOSType(DeviceOSType deviceOSType) {
        this.deviceOSType = deviceOSType;
    }

    public AppBuildType getAppBuildType() {
        return appBuildType;
    }

    public void setAppBuildType(AppBuildType appBuildType) {
        this.appBuildType = appBuildType;
    }

    public Map<String, Object> getPlayerAttributes() {
        return playerAttributes;
    }

    public void setPlayerAttributes(Map<String, Object> playerAttributes) {
        this.playerAttributes = playerAttributes;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnEnterMatchRequest)) return false;
        GameOnEnterMatchRequest that = (GameOnEnterMatchRequest) object;
        return getDeviceOSType() == that.getDeviceOSType() &&
                getAppBuildType() == that.getAppBuildType() &&
                Objects.equals(getPlayerAttributes(), that.getPlayerAttributes()) &&
                Objects.equals(getMatch(), that.getMatch());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceOSType(), getAppBuildType(), getPlayerAttributes(), getMatch());
    }

    @Override
    public String toString() {
        return "GameOnEnterMatchRequest{" +
                "deviceOSType=" + deviceOSType +
                ", appBuildType=" + appBuildType +
                ", playerAttributes=" + playerAttributes +
                ", match=" + match +
                '}';
    }

}
