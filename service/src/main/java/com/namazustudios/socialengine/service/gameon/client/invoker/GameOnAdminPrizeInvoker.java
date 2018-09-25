package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;

public interface GameOnAdminPrizeInvoker {

    GameOnGetPrizeListResponse getPrizes();

    GameOnAddPrizeListResponse addPrizes(GameOnAddPrizeListRequest gameOnAddPrizeListRequest);

    interface Builder extends AdminRequestBuilder<GameOnAdminPrizeInvoker> {}

}
