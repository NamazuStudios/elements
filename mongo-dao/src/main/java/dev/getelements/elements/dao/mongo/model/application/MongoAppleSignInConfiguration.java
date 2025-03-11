package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.Objects;

@Embedded
public class MongoAppleSignInConfiguration {

    @Property
    private String keyId;

    @Property
    private String teamId;

    @Property
    private String clientId;

    @Property
    private String appleSignInPrivateKey;

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAppleSignInPrivateKey() {
        return appleSignInPrivateKey;
    }

    public void setAppleSignInPrivateKey(String appleSignInPrivateKey) {
        this.appleSignInPrivateKey = appleSignInPrivateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoAppleSignInConfiguration that = (MongoAppleSignInConfiguration) o;
        return Objects.equals(getKeyId(), that.getKeyId()) &&
                Objects.equals(getTeamId(), that.getTeamId()) &&
                Objects.equals(getClientId(), that.getClientId()) &&
                Objects.equals(getAppleSignInPrivateKey(), that.getAppleSignInPrivateKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeyId(), getTeamId(), getClientId(), getAppleSignInPrivateKey());
    }

}
