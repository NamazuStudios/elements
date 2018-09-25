package com.namazustudios.socialengine.service.gameon.client.model;

import java.util.Map;

public class EnterMatchRequest {

    private Map<String, Object> playerAttributes;

    public Map<String, Object> getPlayerAttributes() {
        return playerAttributes;
    }

    public void setPlayerAttributes(Map<String, Object> playerAttributes) {
        this.playerAttributes = playerAttributes;
    }

}
