package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

/** Houses the various parameters required which allow communication with the Steam Web API. */
@Schema(
        description = "Houses the various parameters required which allow communication with " +
                "the Steam Microtransaction API.")
public class SteamApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    /** Creates a new instance. */
    public SteamApplicationConfiguration() {}

    @NotNull
    @Schema(description = "The Steam Publisher Web API Key, used to authenticate server-side calls to the " +
            "Steam Microtransaction API (ISteamMicroTxn).")
    private String publisherKey;

    @NotNull
    @Schema(description = "The Steam AppID for the game as it appears in the Steamworks partner portal.")
    private String appId;

    /**
     * Returns the Steam Publisher Web API Key.
     *
     * @return the publisher key
     */
    public String getPublisherKey() {
        return publisherKey;
    }

    /**
     * Sets the Steam Publisher Web API Key.
     *
     * @param publisherKey the publisher key
     */
    public void setPublisherKey(String publisherKey) {
        this.publisherKey = publisherKey;
    }

    /**
     * Returns the Steam AppID.
     *
     * @return the app ID
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the Steam AppID.
     *
     * @param appId the app ID
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SteamApplicationConfiguration that)) return false;
        return Objects.equals(publisherKey, that.publisherKey) && Objects.equals(appId, that.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publisherKey, appId);
    }

    @Override
    public String toString() {
        return "SteamApplicationConfiguration{" +
                "appId='" + appId + '\'' +
                '}';
    }

}
