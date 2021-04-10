package com.namazustudios.socialengine.model.session;

import java.sql.Timestamp;
import java.util.Objects;

public class AppleSignInSession extends Session {

    private String refreshToken;

    private Timestamp refreshTime;

    public Timestamp getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(Timestamp refreshTime) {
        this.refreshTime = refreshTime;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AppleSignInSession that = (AppleSignInSession) o;
        return getRefreshTime() == that.getRefreshTime() &&
                Objects.equals(getRefreshToken(), that.getRefreshToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getRefreshTime(), getRefreshToken());
    }

    @Override
    public String toString() {
        return "AppleSignInSession{" +
                "refreshTime=" + refreshTime +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }

}
