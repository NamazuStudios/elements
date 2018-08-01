package com.namazustudios.socialengine.service.gameon.client.model;

import java.util.Map;

public class EnterTournamentRequest {

    private String accessKey;

    private Map<String, String> playerAttributes;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Map<String, String> getPlayerAttributes() {
        return playerAttributes;
    }

    public void setPlayerAttributes(Map<String, String> playerAttributes) {
        this.playerAttributes = playerAttributes;
    }

}
