package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;

@Schema
public class FirebaseSessionRequest {

    @NotNull
    @Schema
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
