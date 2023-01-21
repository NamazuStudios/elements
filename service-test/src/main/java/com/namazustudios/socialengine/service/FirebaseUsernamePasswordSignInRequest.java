package com.namazustudios.socialengine.service;

import java.util.Objects;

public class FirebaseUsernamePasswordSignInRequest {

    private String email;

    private String password;

    private boolean returnSecureToken = true;

    public FirebaseUsernamePasswordSignInRequest() {}

    public FirebaseUsernamePasswordSignInRequest(final FirebaseEmailPasswordSignUpRequest request) {
        email = request.getEmail();
        password = request.getPassword();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isReturnSecureToken() {
        return returnSecureToken;
    }

    public void setReturnSecureToken(boolean returnSecureToken) {
        this.returnSecureToken = returnSecureToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirebaseUsernamePasswordSignInRequest that = (FirebaseUsernamePasswordSignInRequest) o;
        return isReturnSecureToken() == that.isReturnSecureToken() && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getPassword(), isReturnSecureToken());
    }
}

