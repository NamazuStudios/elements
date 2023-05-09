package dev.getelements.elements.service;

import java.util.Objects;

public class FirebaseDeleteAccountRequest {

    private String idToken;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirebaseDeleteAccountRequest that = (FirebaseDeleteAccountRequest) o;
        return Objects.equals(getIdToken(), that.getIdToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdToken());
    }

}
