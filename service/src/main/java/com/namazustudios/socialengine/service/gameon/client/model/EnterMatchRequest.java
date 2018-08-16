package com.namazustudios.socialengine.service.gameon.client.model;

import java.util.Map;

public class EnterMatchRequest {

    private Map<String, String> playerAttributes;

    public Map<String, String> getPlayerAttributes() {
        return playerAttributes;
    }

    public void setPlayerAttributes(Map<String, String> playerAttributes) {
        this.playerAttributes = playerAttributes;
    }

}
