package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity(value = "session", useDiscriminator = false)
public class MongoGoogleSignInSession extends MongoSession {

    @Property
    private String appleSignInRefreshToken;

    @Property
    private Timestamp appleSignInRefreshTime;

    public String getGoogleSignInRefreshToken() {
        return appleSignInRefreshToken;
    }

    public void setGoogleSignInRefreshToken(String appleSignInRefreshToken) {
        this.appleSignInRefreshToken = appleSignInRefreshToken;
    }

    public Timestamp getGoogleSignInRefreshTime() {
        return appleSignInRefreshTime;
    }

    public void setGoogleSignInRefreshTime(final Timestamp appleSignInRefreshTime) {
        this.appleSignInRefreshTime = appleSignInRefreshTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoGoogleSignInSession that = (MongoGoogleSignInSession) o;
        return getGoogleSignInRefreshTime() == that.getGoogleSignInRefreshTime() &&
                Objects.equals(getGoogleSignInRefreshToken(), that.getGoogleSignInRefreshToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGoogleSignInRefreshTime(), getGoogleSignInRefreshToken());
    }

    @Override
    public String toString() {
        return "MongoGoogleSignInSession{" +
                "refreshTime=" + appleSignInRefreshTime +
                ", refreshToken='" + appleSignInRefreshToken + '\'' +
                '}';
    }

}
