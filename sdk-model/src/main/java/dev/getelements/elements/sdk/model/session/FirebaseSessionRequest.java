package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;

/** Represents a request to create a session using a Firebase JWT. */
@Schema
public class FirebaseSessionRequest {

    /** Creates a new instance. */
    public FirebaseSessionRequest() {}

    @NotNull
    @Schema
    private String firebaseJWT;

    /**
     * Returns the Firebase JWT.
     *
     * @return the Firebase JWT
     */
    public String getFirebaseJWT() {
        return firebaseJWT;
    }

    /**
     * Sets the Firebase JWT.
     *
     * @param firebaseJWT the Firebase JWT
     */
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
