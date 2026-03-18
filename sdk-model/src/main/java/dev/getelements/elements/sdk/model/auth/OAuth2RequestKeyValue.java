package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/** Represents a key-value pair used in an OAuth2 validation request (header, query param, or body field). */
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

    /** Creates a new instance. */
    public OAuth2RequestKeyValue() {}

    /**
     * Creates a new instance with the given key, value, and fromClient flag.
     *
     * @param key the key
     * @param value the value
     * @param fromClient true if the value should come from the client
     */
    public OAuth2RequestKeyValue(String key, String value, boolean fromClient) {
        this.key = key;
        this.value = value;
        this.fromClient = fromClient;
        this.userId = false;
    }

    /**
     * Creates a new instance with all fields.
     *
     * @param key the key
     * @param value the value
     * @param fromClient true if the value should come from the client
     * @param userId true if this parameter's resolved value is the external user identifier
     */
    public OAuth2RequestKeyValue(String key, String value, boolean fromClient, boolean userId) {
        this.key = key;
        this.value = value;
        this.fromClient = fromClient;
        this.userId = userId;
    }

    /**
     * Returns the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns whether the value should be received from the client.
     *
     * @return true if the value comes from the client
     */
    public boolean isFromClient() {
        return fromClient;
    }

    /**
     * Sets whether the value should be received from the client.
     *
     * @param fromClient true if the value comes from the client
     */
    public void setFromClient(boolean fromClient) {
        this.fromClient = fromClient;
    }

    /**
     * Returns whether this parameter's resolved value is the external user identifier.
     *
     * @return true if this is the user ID parameter
     */
    public boolean isUserId() {
        return userId;
    }

    /**
     * Sets whether this parameter's resolved value is the external user identifier.
     *
     * @param userId true if this is the user ID parameter
     */
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
