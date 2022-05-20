package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

/**
 * Provides interaction with GameOn for prizes from the User/Player perspective.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.gameon.prize")
})
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

    /**
     * Gets prize details for the given {@param prizeId}.
     *
     * @param prizeId
     * @return
     */
    GameOnGetPrizeDetailsResponse getDetails(String prizeId, DeviceOSType deviceOSType, AppBuildType appBuildType);

}
