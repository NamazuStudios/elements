package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnGamePrizeInvoker;

import javax.ws.rs.client.Client;

public class V1StandardSecurityGamePrizeInvoker implements GameOnGamePrizeInvoker {

    private static final String PRIZES_PATH = "prizes";

    private static final String CLAIM_PATH = "claim";

    private static final String FULFILL_PATH = "fulfill";

    private final Client client;

    private final GameOnSession gameOnSession;

    public V1StandardSecurityGamePrizeInvoker(final Client client, final GameOnSession gameOnSession) {
        this.client = client;
        this.gameOnSession = gameOnSession;
    }

    @Override
    public GameOnClaimPrizeListResponse claim(GameOnClaimPrizeListRequest request) {
        // TODO Implement
        return null;
    }

    @Override
    public GameOnFulfillPrizeListResponse fulfill(GameOnFulfillPrizeRequest request) {
        // TODO Implement
        return null;
    }

}
