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

@ApiModel(description = "Represents a GameOn session stored and managed by Elements.  This is designed to be a 1:1 " +
                        "overlap with the Amazon GameOn API with additional Elements add-ons.")
public class GameOnSession implements Serializable {

    @ApiModelProperty("The Elements assigned session ID.")
    @Null(groups = {Insert.class, Create.class})
    private String id;

    @NotNull(groups = {Create.class, Insert.class})
    @ApiModelProperty("The Device Operating System type.")
    private DeviceOSType deviceOSType;

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
    private Long sessionExpirationDate;

    @NotNull(groups = Insert.class)
    @ApiModelProperty("The profile that owns this particualr session.")
    private Profile profile;

    @NotNull(groups = {Create.class, Insert.class})
    @ApiModelProperty("The appliaction build type.")
    private AppBuildType appBuildType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeviceOSType getDeviceOSType() {
        return deviceOSType;
    }

    public void setDeviceOSType(DeviceOSType deviceOSType) {
        this.deviceOSType = deviceOSType;
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

    public Long getSessionExpirationDate() {
        return sessionExpirationDate;
    }

    public void setSessionExpirationDate(Long sessionExpirationDate) {
        this.sessionExpirationDate = sessionExpirationDate;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public AppBuildType getAppBuildType() {
        return appBuildType;
    }

    public void setAppBuildType(AppBuildType appBuildType) {
        this.appBuildType = appBuildType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnSession)) return false;
        GameOnSession that = (GameOnSession) object;
        return Objects.equals(getId(), that.getId()) &&
                getDeviceOSType() == that.getDeviceOSType() &&
                Objects.equals(getSessionId(), that.getSessionId()) &&
                Objects.equals(getSessionApiKey(), that.getSessionApiKey()) &&
                Objects.equals(getSessionExpirationDate(), that.getSessionExpirationDate()) &&
                Objects.equals(getProfile(), that.getProfile()) &&
                getAppBuildType() == that.getAppBuildType();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getDeviceOSType(), getSessionId(), getSessionApiKey(), getSessionExpirationDate(), getProfile(), getAppBuildType());
    }

    @Override
    public String toString() {
        return "GameOnSession{" +
                "id='" + id + '\'' +
                ", deviceOSType=" + deviceOSType +
                ", sessionId='" + sessionId + '\'' +
                ", sessionApiKey='" + sessionApiKey + '\'' +
                ", sessionExpirationDate=" + sessionExpirationDate +
                ", profile=" + profile +
                ", appBuildType=" + appBuildType +
                '}';
    }

}
