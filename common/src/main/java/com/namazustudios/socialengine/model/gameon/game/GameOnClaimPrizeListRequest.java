package com.namazustudios.socialengine.model.gameon.game;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description = "Corresponds to the GameOn Claim Prize List Request:  " +
                        "https://developer.amazon.com/docs/gameon/game-api-ref.html#claimprizelistrequest")
public class GameOnClaimPrizeListRequest {

    @ApiModelProperty("Specifes the device OS Type.")
    private DeviceOSType deviceOSType;

    @ApiModelProperty("Specifies the application build type.")
    private AppBuildType appBuildType;

    @ApiModelProperty("A list of awarded prize IDs.")
    private List<String> awardedPrizeIds;

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

    public List<String> getAwardedPrizeIds() {
        return awardedPrizeIds;
    }

    public void setAwardedPrizeIds(List<String> awardedPrizeIds) {
        this.awardedPrizeIds = awardedPrizeIds;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnClaimPrizeListRequest)) return false;
        GameOnClaimPrizeListRequest that = (GameOnClaimPrizeListRequest) object;
        return getDeviceOSType() == that.getDeviceOSType() &&
                getAppBuildType() == that.getAppBuildType() &&
                Objects.equals(getAwardedPrizeIds(), that.getAwardedPrizeIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceOSType(), getAppBuildType(), getAwardedPrizeIds());
    }

    @Override
    public String toString() {
        return "GameOnClaimPrizeListRequest{" +
                "deviceOSType=" + deviceOSType +
                ", appBuildType=" + appBuildType +
                ", awardedPrizeIds=" + awardedPrizeIds +
                '}';
    }

}
