package com.namazustudios.socialengine.service.gameon.client.model;

import com.namazustudios.socialengine.model.gameon.AppBuildType;
import com.namazustudios.socialengine.model.gameon.DeviceOSType;

public class AuthPlayerRequest {

    private AppBuildType appBuildType;

    private DeviceOSType deviceOSType;

    private String playerToken;

    private String playerName;

    public AppBuildType getAppBuildType() {
        return appBuildType;
    }

    public void setAppBuildType(AppBuildType appBuildType) {
        this.appBuildType = appBuildType;
    }

    public DeviceOSType getDeviceOSType() {
        return deviceOSType;
    }

    public void setDeviceOSType(DeviceOSType deviceOSType) {
        this.deviceOSType = deviceOSType;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String playerToken) {
        this.playerToken = playerToken;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

}
