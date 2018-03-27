package com.namazustudios.socialengine.model.notification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Represents a
 */
@ApiModel("Represents a Firebase Cloud Messaging Registration Token")
public class FCMRegistrationToken {

    @ApiModelProperty("The the unique id of the token stored in the database.")
    private String id;

    @NotNull
    @ApiModelProperty("The actual Firebase registration token.")
    private String registrationToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FCMRegistrationToken)) return false;

        FCMRegistrationToken that = (FCMRegistrationToken) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        return getRegistrationToken() != null ? getRegistrationToken().equals(that.getRegistrationToken()) : that.getRegistrationToken() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getRegistrationToken() != null ? getRegistrationToken().hashCode() : 0);
        return result;
    }

}
