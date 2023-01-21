package com.namazustudios.socialengine.service;

import java.util.Objects;

public class FirebaseEmailPasswordSignUpResponse {

    private String kind;

    private String idToken;

    private String email;

    private String refreshToken;

    private String expiresIn;

    private String localId;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirebaseEmailPasswordSignUpResponse that = (FirebaseEmailPasswordSignUpResponse) o;
        return Objects.equals(getIdToken(), that.getIdToken()) && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getRefreshToken(), that.getRefreshToken()) && Objects.equals(getExpiresIn(), that.getExpiresIn()) && Objects.equals(getLocalId(), that.getLocalId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdToken(), getEmail(), getRefreshToken(), getExpiresIn(), getLocalId());
    }

}
