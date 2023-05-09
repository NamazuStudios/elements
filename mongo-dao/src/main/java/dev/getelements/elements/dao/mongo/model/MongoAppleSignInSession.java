package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity(value = "session", useDiscriminator = false)
public class MongoAppleSignInSession extends MongoSession {

    @Property
    private String appleSignInRefreshToken;

    @Property
    private Timestamp appleSignInRefreshTime;

    public String getAppleSignInRefreshToken() {
        return appleSignInRefreshToken;
    }

    public void setAppleSignInRefreshToken(String appleSignInRefreshToken) {
        this.appleSignInRefreshToken = appleSignInRefreshToken;
    }

    public Timestamp getAppleSignInRefreshTime() {
        return appleSignInRefreshTime;
    }

    public void setAppleSignInRefreshTime(final Timestamp appleSignInRefreshTime) {
        this.appleSignInRefreshTime = appleSignInRefreshTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoAppleSignInSession that = (MongoAppleSignInSession) o;
        return getAppleSignInRefreshTime() == that.getAppleSignInRefreshTime() &&
                Objects.equals(getAppleSignInRefreshToken(), that.getAppleSignInRefreshToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppleSignInRefreshTime(), getAppleSignInRefreshToken());
    }

    @Override
    public String toString() {
        return "MongoAppleSignInSession{" +
                "refreshTime=" + appleSignInRefreshTime +
                ", refreshToken='" + appleSignInRefreshToken + '\'' +
                '}';
    }

}
