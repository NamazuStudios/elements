package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.GameOnGamePrizeService;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.ProfileService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnGamePrizeInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.ClaimPrizeListRequest;
import com.namazustudios.socialengine.service.gameon.client.model.FulfillPrizeListRequest;

import javax.inject.Inject;
import javax.inject.Provider;

public class UserGameOnGamePrizeService implements GameOnGamePrizeService {

    private GameOnSessionService gameOnSessionService;

    private Provider<GameOnGamePrizeInvoker.Builder> gameOnGamePrizeInvokerBuilderProvider;

    @Override
    public GameOnClaimPrizeListResponse claim(final GameOnClaimPrizeListRequest gameOnClaimPrizeListRequest) {

        final DeviceOSType deviceOSType = gameOnClaimPrizeListRequest.getDeviceOSType() == null ?
            DeviceOSType.getDefault() :
            gameOnClaimPrizeListRequest.getDeviceOSType();

        final AppBuildType appBuildType = gameOnClaimPrizeListRequest.getAppBuildType() == null ?
            AppBuildType.getDefault() :
            gameOnClaimPrizeListRequest.getAppBuildType();

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final ClaimPrizeListRequest claimPrizeListRequest = new ClaimPrizeListRequest();
        claimPrizeListRequest.setAwardedPrizeIds(gameOnClaimPrizeListRequest.getAwardedPrizeIds());

        return getGameOnGamePrizeInvokerBuilderProvider()
            .get()
            .withSession(gameOnSession)
            .withExpirationRetry(e -> getGameOnSessionService().refreshExpiredSession(e.getExpired()))
            .build()
            .claim(claimPrizeListRequest);

    }

    @Override
    public GameOnFulfillPrizeListResponse fulfill(final GameOnFulfillPrizeRequest gameOnFulfillPrizeRequest) {

        final DeviceOSType deviceOSType = gameOnFulfillPrizeRequest.getDeviceOSType() == null ?
                DeviceOSType.getDefault() :
                gameOnFulfillPrizeRequest.getDeviceOSType();

        final AppBuildType appBuildType = gameOnFulfillPrizeRequest.getAppBuildType() == null ?
                AppBuildType.getDefault() :
                gameOnFulfillPrizeRequest.getAppBuildType();

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final FulfillPrizeListRequest fulfillPrizeListRequest = new FulfillPrizeListRequest();
        fulfillPrizeListRequest.setAwardedPrizeIds(gameOnFulfillPrizeRequest.getAwardedPrizeIds());

        return getGameOnGamePrizeInvokerBuilderProvider()
                .get()
                .withSession(gameOnSession)
                .withExpirationRetry(e -> getGameOnSessionService().refreshExpiredSession(e.getExpired()))
                .build()
                .fulfill(fulfillPrizeListRequest);

    }

    public GameOnSessionService getGameOnSessionService() {
        return gameOnSessionService;
    }

    @Inject
    public void setGameOnSessionService(GameOnSessionService gameOnSessionService) {
        this.gameOnSessionService = gameOnSessionService;
    }

    public Provider<GameOnGamePrizeInvoker.Builder> getGameOnGamePrizeInvokerBuilderProvider() {
        return gameOnGamePrizeInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnGamePrizeInvokerBuilderProvider(Provider<GameOnGamePrizeInvoker.Builder> gameOnGamePrizeInvokerBuilderProvider) {
        this.gameOnGamePrizeInvokerBuilderProvider = gameOnGamePrizeInvokerBuilderProvider;
    }

}

