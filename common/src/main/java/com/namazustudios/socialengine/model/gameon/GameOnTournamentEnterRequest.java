package com.namazustudios.socialengine.model.gameon;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.match.Match;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

@ApiModel(description = "The data required to post a new entry into a tournament.")
public class GameOnTournamentEnterRequest {

    @ApiModelProperty("The device OS Type, used to create or reference the session.")
    private DeviceOSType deviceOSType;

    @ApiModelProperty("App build type, used to create or reference the session.")
    private AppBuildType appBuildType;

    @ApiModelProperty("An optional access key to enter the tournament.  Only used if the tournament was created with " +
                      "a special code requiring entry.")
    private String accessKey;

    @ApiModelProperty("The player attribues, if applicable.  This may be empty or null.")
    private Map<String, String> playerAttributes;

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

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Map<String, String> getPlayerAttributes() {
        return playerAttributes;
    }

    public void setPlayerAttributes(Map<String, String> playerAttributes) {
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
        if (!(object instanceof GameOnTournamentEnterRequest)) return false;
        GameOnTournamentEnterRequest that = (GameOnTournamentEnterRequest) object;
        return getDeviceOSType() == that.getDeviceOSType() &&
                getAppBuildType() == that.getAppBuildType() &&
                Objects.equals(getAccessKey(), that.getAccessKey()) &&
                Objects.equals(getPlayerAttributes(), that.getPlayerAttributes()) &&
                Objects.equals(getMatch(), that.getMatch());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceOSType(), getAppBuildType(), getAccessKey(), getPlayerAttributes(), getMatch());
    }

    @Override
    public String toString() {
        return "GameOnTournamentEnterRequest{" +
                "deviceOSType=" + deviceOSType +
                ", appBuildType=" + appBuildType +
                ", accessKey='" + accessKey + '\'' +
                ", playerAttributes=" + playerAttributes +
                ", match=" + match +
                '}';
    }

}
