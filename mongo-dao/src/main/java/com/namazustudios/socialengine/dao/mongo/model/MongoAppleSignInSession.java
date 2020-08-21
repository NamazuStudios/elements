package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.*;

import java.util.Objects;

@Entity(value = "session", noClassnameStored = true)
public class MongoAppleSignInSession extends MongoSession {

    @Property
    private long refreshTime;

    @Property
    private String refreshToken;

    public long getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(long refreshTime) {
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
        MongoAppleSignInSession that = (MongoAppleSignInSession) o;
        return getRefreshTime() == that.getRefreshTime() &&
                Objects.equals(getRefreshToken(), that.getRefreshToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRefreshTime(), getRefreshToken());
    }

    @Override
    public String toString() {
        return "MongoAppleSignInSession{" +
                "refreshTime=" + refreshTime +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }

}
