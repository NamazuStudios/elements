package com.namazustudios.socialengine.service.gameon.client.model;

public class RegisterPlayerResponse {

    private String playerToken;

    private String encryptedPlayerToken;

    private String externalPlayerId;

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String playerToken) {
        this.playerToken = playerToken;
    }

    public String getEncryptedPlayerToken() {
        return encryptedPlayerToken;
    }

    public void setEncryptedPlayerToken(String encryptedPlayerToken) {
        this.encryptedPlayerToken = encryptedPlayerToken;
    }

    public String getExternalPlayerId() {
        return externalPlayerId;
    }

    public void setExternalPlayerId(String externalPlayerId) {
        this.externalPlayerId = externalPlayerId;
    }

}
