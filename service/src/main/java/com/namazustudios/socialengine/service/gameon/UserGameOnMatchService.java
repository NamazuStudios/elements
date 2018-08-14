package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.GameOnMatchService;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;

import javax.inject.Inject;
import javax.inject.Provider;


public class UserGameOnMatchService implements GameOnMatchService {

    private GameOnSessionService gameOnSessionService;

    private Provider<GameOnMatchInvoker.Builder> gameOnMatchInvokerProvider;

    @Override
    public GameOnMatchesAggregate getMatches(
            final DeviceOSType deviceOSType, final AppBuildType appBuildType,
            final MatchFilter filterBy, final MatchType matchType,
            final TournamentPeriod period, final String playerAttributes) {

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        return getGameOnMatchInvokerProvider()
            .get()
            .withSession(gameOnSession)
            .build()
            .getSummaries(filterBy, matchType, period, playerAttributes);

    }

    @Override
    public GameOnMatchDetail getMatch(
            final DeviceOSType deviceOSType, final AppBuildType appBuildType,
            final String playerAttributes, final String matchId) {

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        return getGameOnMatchInvokerProvider()
                .get()
                .withSession(gameOnSession)
                .build()
                .getDetail(matchId, playerAttributes);

    }

    public GameOnSessionService getGameOnSessionService() {
        return gameOnSessionService;
    }

    @Inject
    public void setGameOnSessionService(GameOnSessionService gameOnSessionService) {
        this.gameOnSessionService = gameOnSessionService;
    }

    public Provider<GameOnMatchInvoker.Builder> getGameOnMatchInvokerProvider() {
        return gameOnMatchInvokerProvider;
    }

    @Inject
    public void setGameOnMatchInvokerProvider(Provider<GameOnMatchInvoker.Builder> gameOnMatchInvokerProvider) {
        this.gameOnMatchInvokerProvider = gameOnMatchInvokerProvider;
    }

}
