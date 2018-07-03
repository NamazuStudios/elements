package com.namazustudios.socialengine.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class GameOnApplicationConfiguration extends ApplicationConfiguration {

    @NotNull
    @ApiModelProperty("The Amazon Assigned Game ID.  This mirrors the unique identifier of the configuration.")
    private String gameId;

    @NotNull
    @ApiModelProperty("The public API key for the application.  This is safe to share with end-users and otherwise " +
                      "untrusted clients.")
    private String publicApiKey;

    @NotNull
    @ApiModelProperty("The admin API key for the application.  This is secret and should only be shared with trusted " +
                      "administrator users.")
    private String adminApiKey;

    @NotNull
    @ApiModelProperty("The public key for signing requests.")
    private String publicKey;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPublicApiKey() {
        return publicApiKey;
    }

    public void setPublicApiKey(String publicApiKey) {
        this.publicApiKey = publicApiKey;
    }

    public String getAdminApiKey() {
        return adminApiKey;
    }

    public void setAdminApiKey(String adminApiKey) {
        this.adminApiKey = adminApiKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "GameOnApplicationConfiguration{" +
                "gameId='" + gameId + '\'' +
                ", publicApiKey='" + publicApiKey + '\'' +
                ", adminApiKey='" + adminApiKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnApplicationConfiguration)) return false;
        if (!super.equals(object)) return false;
        GameOnApplicationConfiguration that = (GameOnApplicationConfiguration) object;
        return Objects.equals(getGameId(), that.getGameId()) &&
                Objects.equals(getPublicApiKey(), that.getPublicApiKey()) &&
                Objects.equals(getAdminApiKey(), that.getAdminApiKey()) &&
                Objects.equals(getPublicKey(), that.getPublicKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGameId(), getPublicApiKey(), getAdminApiKey(), getPublicKey());
    }

}
