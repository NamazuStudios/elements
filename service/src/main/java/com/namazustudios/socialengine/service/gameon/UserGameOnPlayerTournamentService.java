package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.gameon.GameOnTournamentNotFoundException;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnPlayerTournamentEnterRequest;
import com.namazustudios.socialengine.model.gameon.GameOnPlayerTournamentEnterResponse;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnPlayerTournamentService;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.MatchServiceUtils;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnPlayerTournamentInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.EnterPlayerTournamentRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.GameOnConstants.MATCH_METADATA_MATCH_ID;
import static com.namazustudios.socialengine.GameOnConstants.MATCH_METADATA_TOURNAMENT_ID;
import static com.namazustudios.socialengine.model.gameon.game.MatchFilter.live;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class UserGameOnPlayerTournamentService implements GameOnPlayerTournamentService {

    private MatchDao matchDao;

    private MatchServiceUtils matchServiceUtils;

    private MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao;

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
            .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
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
            .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
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
            .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
            .build()
            .getSummaries(live, MatchType.player_generated, TournamentPeriod.all, playerAttributes);

        return gameOnMatchesAggregate
            .getMatches()
            .stream()
            .map(s -> s.getTournamentId())
            .collect(toSet());

    }

    @Override
    public GameOnPlayerTournamentEnterResponse enterTournament(
            final String tournamentId,
            final GameOnPlayerTournamentEnterRequest gameOnPlayerTournamentEnterRequest) {

        final Profile profile = getCurrentProfileSupplier().get();

        final DeviceOSType deviceOSType = gameOnPlayerTournamentEnterRequest.getDeviceOSType() == null ?
                DeviceOSType.getDefault()                              :
                gameOnPlayerTournamentEnterRequest.getDeviceOSType();

        final AppBuildType appBuildType = gameOnPlayerTournamentEnterRequest.getAppBuildType() == null ?
                AppBuildType.getDefault()                              :
                gameOnPlayerTournamentEnterRequest.getAppBuildType();

        final Match match = gameOnPlayerTournamentEnterRequest.getMatch();

        final MatchmakingApplicationConfiguration configuration = getMatchmakingApplicationConfigurationDao()
                .getApplicationConfiguration(profile.getApplication().getId(), match.getScheme());

        if (match.getPlayer() == null) {
            match.setPlayer(profile);
        } else if (!Objects.equals(profile, match.getPlayer())) {
            throw new ForbiddenException("player must match current profile");
        }

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final EnterPlayerTournamentRequest enterPlayerTournamentRequest;
        enterPlayerTournamentRequest = new EnterPlayerTournamentRequest();
        enterPlayerTournamentRequest.setAccessKey(gameOnPlayerTournamentEnterRequest.getAccessKey());

        final GameOnPlayerTournamentEnterResponse response = getGameOnPlayerTournamentInvokerBuilderProvider()
                .get()
                .withSession(gameOnSession)
                .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
                .build()
                .postEnterRequest(tournamentId, enterPlayerTournamentRequest);

        match.setScope(response.getTournamentId());

        final Map<String, Serializable> metadata = new HashMap<>();
        metadata.put(MATCH_METADATA_MATCH_ID, response.getMatchId());
        metadata.put(MATCH_METADATA_TOURNAMENT_ID, response.getTournamentId());
        match.setMetadata(metadata);

        final Match inserted = getMatchDao().createMatch(match);

        final Matchmaker matchmaker = getMatchDao()
                .getMatchmaker(configuration.getAlgorithm())
                .withScope(response.getTournamentId());

        final Match paired = getMatchServiceUtils().attempt(matchmaker, inserted, configuration);
        response.setMatch(paired);

        return response;

    }

    public MatchDao getMatchDao() {
        return matchDao;
    }

    @Inject
    public void setMatchDao(MatchDao matchDao) {
        this.matchDao = matchDao;
    }

    public MatchServiceUtils getMatchServiceUtils() {
        return matchServiceUtils;
    }

    @Inject
    public void setMatchServiceUtils(MatchServiceUtils matchServiceUtils) {
        this.matchServiceUtils = matchServiceUtils;
    }

    public MatchmakingApplicationConfigurationDao getMatchmakingApplicationConfigurationDao() {
        return matchmakingApplicationConfigurationDao;
    }

    @Inject
    public void setMatchmakingApplicationConfigurationDao(MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao) {
        this.matchmakingApplicationConfigurationDao = matchmakingApplicationConfigurationDao;
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
