package dev.getelements.elements.model.googlesignin;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.APPLE_ITUNES;

public class TokenResponse {

    private String accessToken;

    private Long expiresAt;

    private String idToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

}
