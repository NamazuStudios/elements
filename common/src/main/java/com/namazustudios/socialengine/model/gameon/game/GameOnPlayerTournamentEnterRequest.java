package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.match.Match;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Contains the information necessary to enter a GameOn player tournament.")
public class GameOnPlayerTournamentEnterRequest implements Serializable {

    @ApiModelProperty("The device OS Type, used to create or reference the session.")
    private DeviceOSType deviceOSType;

    @ApiModelProperty("App build type, used to create or reference the session.")
    private AppBuildType appBuildType;

    @ApiModelProperty("The player-defined access key for the tournament.  Only necessary if the player creating the " +
                      "tournament specified an access key.")
    private String accessKey;

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

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
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
        if (!(object instanceof GameOnPlayerTournamentEnterRequest)) return false;
        GameOnPlayerTournamentEnterRequest that = (GameOnPlayerTournamentEnterRequest) object;
        return getDeviceOSType() == that.getDeviceOSType() &&
                getAppBuildType() == that.getAppBuildType() &&
                Objects.equals(getAccessKey(), that.getAccessKey()) &&
                Objects.equals(getMatch(), that.getMatch());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceOSType(), getAppBuildType(), getAccessKey(), getMatch());
    }

    @Override
    public String toString() {
        return "GameOnPlayerTournamentEnterRequest{" +
                "deviceOSType=" + deviceOSType +
                ", appBuildType=" + appBuildType +
                ", accessKey='" + accessKey + '\'' +
                ", match=" + match +
                '}';
    }

}
