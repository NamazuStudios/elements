package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.namazustudios.socialengine.client.rest.client.gameon.GameOnPrizesClient;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.admin.GetPrizeListResponse;

import javax.inject.Inject;

public class PrizeDataProvider extends AsyncDataProvider<GetPrizeListResponse.Prize> {

    @Inject
    private GameOnPrizesClient prizesClient;

    private GameOnApplicationConfiguration gameOnApplicationConfiguration;

    @Override
    protected void onRangeChanged(final HasData<GetPrizeListResponse.Prize> display) {
        // TODO: Implement this
    }

    public GameOnApplicationConfiguration getGameOnApplicationConfiguration() {
        return gameOnApplicationConfiguration;
    }

    public void reconfigure(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;
    }

}
