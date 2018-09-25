package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.game.GameOnClaimPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.game.GameOnClaimPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnFulfillPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnFulfillPrizeRequest;

public interface GameOnGamePrizeInvoker {

    GameOnClaimPrizeListResponse claim(GameOnClaimPrizeListRequest request);

    GameOnFulfillPrizeListResponse fulfill(GameOnFulfillPrizeRequest request);

    interface Builder extends PlayerRequestBuilder<GameOnGamePrizeInvoker> {}

}
