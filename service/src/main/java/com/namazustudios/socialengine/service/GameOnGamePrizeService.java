package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.game.GameOnClaimPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.game.GameOnClaimPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnFulfillPrizeRequest;
import com.namazustudios.socialengine.model.gameon.game.GameOnFulfillPrizeListResponse;

/**
 * Provides interaction with GameOn for prizes from the User/Player perspective.
 */
public interface GameOnGamePrizeService {

    /**
     * Claims all prizes contained in the supplied {@link GameOnClaimPrizeListRequest}.
     *
     * @param gameOnClaimPrizeListRequest the {@link GameOnClaimPrizeListRequest}
     * @return an instance of {@link GameOnClaimPrizeListResponse}
     */
    GameOnClaimPrizeListResponse claim(GameOnClaimPrizeListRequest gameOnClaimPrizeListRequest);

    /**
     * Fulfills all prizes contains in the supplied {@link GameOnFulfillPrizeRequest}.
     *
     * @param gameOnFulfillPrizeRequest
     * @return an instance of {@link GameOnFulfillPrizeListResponse}
     */
    GameOnFulfillPrizeListResponse fulfill(GameOnFulfillPrizeRequest gameOnFulfillPrizeRequest);

}
