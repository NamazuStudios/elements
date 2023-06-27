package dev.getelements.elements.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel
public class FirebaseSessionRequest {

    @NotNull
    @ApiModelProperty
    private String firebaseJWT;

    public String getFirebaseJWT() {
        return firebaseJWT;
    }

    public void setFirebaseJWT(String firebaseJWT) {
        this.firebaseJWT = firebaseJWT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FirebaseSessionRequest{");
        sb.append("firebaseJWT='").append("<redacted>").append('\'');
        sb.append('}');
        return sb.toString();
    }

}
