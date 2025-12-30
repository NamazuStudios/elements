package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

public class OAuth2RequestKeyValue implements Serializable {

    @Schema(description = "The key.")
    private String key;

    @Schema(description = "The value.")
    private String value;

    @Schema(description = "If this value should be received from the client, or predefined and stored in the DB.")
    private boolean fromClient;

    @Schema(description = """
            If true, this parameter's resolved value is treated as the external user identifier
            to link to your internal user id.
            Only one parameter should have this set to true per scheme.
            """)
    private boolean userId;

    public OAuth2RequestKeyValue() {}

    public OAuth2RequestKeyValue(String key, String value, boolean fromClient) {
        this.key = key;
        this.value = value;
        this.fromClient = fromClient;
        this.userId = false;
    }

    public OAuth2RequestKeyValue(String key, String value, boolean fromClient, boolean userId) {
        this.key = key;
        this.value = value;
        this.fromClient = fromClient;
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isFromClient() {
        return fromClient;
    }

    public void setFromClient(boolean fromClient) {
        this.fromClient = fromClient;
    }

    public boolean isUserId() {
        return userId;
    }

    public void setUserId(boolean userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OAuth2RequestKeyValue that)) return false;
        return fromClient == that.fromClient && userId == that.userId && Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, fromClient, userId);
    }

    @Override
    public String toString() {
        return "OAuth2RequestKeyValue{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", fromClient=" + fromClient +
                ", userId=" + userId +
                '}';
    }
}
