package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.exception.gameon.GameOnTournamentNotFoundException;
import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnPlayerTournamentService;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnPlayerTournamentInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnTournamentInvoker;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.model.gameon.MatchFilter.live;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class UserGameOnPlayerTournamentService implements GameOnPlayerTournamentService {

    private Supplier<Profile> currentProfileSupplier;

    private GameOnSessionService gameOnSessionService;

    private Provider<GameOnMatchInvoker.Builder> gameOnMatchInvokerBuilderProvider;

    private Provider<GameOnPlayerTournamentInvoker.Builder> gameOnPlayerTournamentInvokerBuilderProvider;

    @Override
    public List<GameOnTournamentSummary> getEligibleTournaments(
            final DeviceOSType deviceOSType, final AppBuildType appBuildType,
            final TournamentFilter filterBy, final TournamentPeriod period, final String playerAttributes) {

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final Set<String> enteredTournamentIdSet = getEnteredMatchIds(gameOnSession, playerAttributes);

        final List<GameOnTournamentSummary> gameOnTournamentSummaries = getGameOnPlayerTournamentInvokerBuilderProvider()
            .get()
            .withSession(gameOnSession)
            .build()
            .getSummaries(filterBy, period, playerAttributes);

        return gameOnTournamentSummaries
            .stream()
            .filter(s -> !enteredTournamentIdSet.contains(s.getTournamentId()))
            .collect(toList());

    }

    @Override
    public GameOnTournamentDetail getEligibleTournamentDetail(
            final DeviceOSType deviceOSType, final AppBuildType appBuildType,
            final String playerAttributes, final String tournamentId) {

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final Set<String> enteredTournamentIdSet = getEnteredMatchIds(gameOnSession, playerAttributes);

        final GameOnTournamentDetail gameOnTournamentDetail = getGameOnPlayerTournamentInvokerBuilderProvider()
            .get()
            .withSession(gameOnSession)
            .build()
            .getDetail(playerAttributes, tournamentId);

        if (enteredTournamentIdSet.contains(gameOnTournamentDetail.getTournamentId())) {
            throw new GameOnTournamentNotFoundException("Tournament not found.");
        }

        return gameOnTournamentDetail;

    }

    private Set<String> getEnteredMatchIds(final GameOnSession gameOnSession, final String playerAttributes) {

        final GameOnMatchesAggregate gameOnMatchesAggregate = getGameOnMatchInvokerBuilderProvider()
            .get()
            .withSession(gameOnSession)
            .build()
            .getSummaries(live, MatchType.player_generated, TournamentPeriod.all, playerAttributes);

        return gameOnMatchesAggregate
            .getMatches()
            .stream()
            .map(s -> s.getTournamentId())
            .collect(toSet());

    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    public GameOnSessionService getGameOnSessionService() {
        return gameOnSessionService;
    }

    @Inject
    public void setGameOnSessionService(GameOnSessionService gameOnSessionService) {
        this.gameOnSessionService = gameOnSessionService;
    }

    public Provider<GameOnMatchInvoker.Builder> getGameOnMatchInvokerBuilderProvider() {
        return gameOnMatchInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnMatchInvokerBuilderProvider(Provider<GameOnMatchInvoker.Builder> gameOnMatchInvokerBuilderProvider) {
        this.gameOnMatchInvokerBuilderProvider = gameOnMatchInvokerBuilderProvider;
    }

    public Provider<GameOnPlayerTournamentInvoker.Builder> getGameOnPlayerTournamentInvokerBuilderProvider() {
        return gameOnPlayerTournamentInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnPlayerTournamentInvokerBuilderProvider(Provider<GameOnPlayerTournamentInvoker.Builder> gameOnPlayerTournamentInvokerBuilderProvider) {
        this.gameOnPlayerTournamentInvokerBuilderProvider = gameOnPlayerTournamentInvokerBuilderProvider;
    }

}
