package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.game.GameOnClaimPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnFulfillPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnFulfillPrizeRequest;
import com.namazustudios.socialengine.model.gameon.game.GameOnGetPrizeDetailsResponse;
import com.namazustudios.socialengine.service.gameon.client.model.ClaimPrizeListRequest;
import com.namazustudios.socialengine.service.gameon.client.model.FulfillPrizeListRequest;

public interface GameOnGamePrizeInvoker {

    GameOnClaimPrizeListResponse claim(ClaimPrizeListRequest claimPrizeListRequest);

    GameOnFulfillPrizeListResponse fulfill(FulfillPrizeListRequest fulfillPrizeListRequest);

    GameOnGetPrizeDetailsResponse getDetails(String prizeId);

    interface Builder extends PlayerRequestBuilder<GameOnGamePrizeInvoker> {}


}
