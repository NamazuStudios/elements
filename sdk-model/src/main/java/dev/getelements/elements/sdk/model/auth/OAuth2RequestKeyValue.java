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

    public OAuth2RequestKeyValue() {}

    public OAuth2RequestKeyValue(String key, String value, boolean fromClient) {
        this.key = key;
        this.value = value;
        this.fromClient = fromClient;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuth2RequestKeyValue that = (OAuth2RequestKeyValue) o;
        return Objects.equals(getKey(), that.getKey()) && Objects.equals(getValue(), that.getValue()) && Objects.equals(isFromClient(), that.isFromClient());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue(), isFromClient());
    }
}
