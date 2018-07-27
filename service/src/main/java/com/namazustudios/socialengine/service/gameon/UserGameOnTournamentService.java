package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.GameOnTournamentService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnTournamentInvoker;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.function.Supplier;

public class UserGameOnTournamentService implements GameOnTournamentService {

    private Supplier<Profile> currentProfileSupplier;

    private GameOnSessionService gameOnSessionService;

    private Provider<GameOnTournamentInvoker.Builder> gameOnTournamentInvokerBuilderProvider;

    @Override
    public List<GameOnTournamentSummary> getEligibleTournaments(
            final DeviceOSType deviceOSType,
            final AppBuildType appBuildType,
            final TournamentFilter filterBy,
            final String playerAttributes) {

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final List<GameOnTournamentSummary> gameOnTournamentSummaries = getGameOnTournamentInvokerBuilderProvider()
            .get()
            .withSession(gameOnSession)
            .build()
            .getSummaries(filterBy, playerAttributes);

        return null;
    }

    @Override
    public GameOnTournamentDetail getEligibleTournamentDetail(
            final String tournamentId,
            final DeviceOSType deviceOSType,
            final AppBuildType appBuildType,
            final TournamentFilter filterBy,
            final String playerAttributes) {

        return null;
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

    public Provider<GameOnTournamentInvoker.Builder> getGameOnTournamentInvokerBuilderProvider() {
        return gameOnTournamentInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnTournamentInvokerBuilderProvider(Provider<GameOnTournamentInvoker.Builder> gameOnTournamentInvokerBuilderProvider) {
        this.gameOnTournamentInvokerBuilderProvider = gameOnTournamentInvokerBuilderProvider;
    }

}
