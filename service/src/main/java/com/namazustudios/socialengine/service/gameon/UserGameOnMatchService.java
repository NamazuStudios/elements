package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnEnterMatchRequest;
import com.namazustudios.socialengine.model.gameon.GameOnEnterMatchResponse;
import com.namazustudios.socialengine.model.gameon.TournamentEntryMetadata;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Serialize;
import com.namazustudios.socialengine.service.GameOnMatchService;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.MatchServiceUtils;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.EnterMatchRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.GameOnConstants.TOURNAMENT_ENTRY_METADATA_KEY;


public class UserGameOnMatchService implements GameOnMatchService {

    private MatchDao matchDao;

    private MatchServiceUtils matchServiceUtils;

    private GameOnSessionService gameOnSessionService;

    private Supplier<Profile> currentProfileSupplier;

    private Provider<GameOnMatchInvoker.Builder> gameOnMatchInvokerProvider;

    private MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao;

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
            .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
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
            .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
            .build()
            .getDetail(matchId, playerAttributes);

    }

    @Override
    public GameOnEnterMatchResponse enterMatch(
            final String matchId,
            final GameOnEnterMatchRequest gameOnEnterMatchRequest) {

        final Profile profile = getCurrentProfileSupplier().get();

        final DeviceOSType deviceOSType = gameOnEnterMatchRequest.getDeviceOSType() == null ?
                DeviceOSType.getDefault()                              :
                gameOnEnterMatchRequest.getDeviceOSType();

        final AppBuildType appBuildType = gameOnEnterMatchRequest.getAppBuildType() == null ?
                AppBuildType.getDefault()                              :
                gameOnEnterMatchRequest.getAppBuildType();

        final Match match = gameOnEnterMatchRequest.getMatch();

        final MatchmakingApplicationConfiguration configuration = getMatchmakingApplicationConfigurationDao()
                .getApplicationConfiguration(profile.getApplication().getId(), match.getScheme());

        if (match.getPlayer() == null) {
            match.setPlayer(profile);
        } else if (!Objects.equals(profile, match.getPlayer())) {
            throw new ForbiddenException("player must match current profile");
        }

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final EnterMatchRequest enterMatchRequest = new EnterMatchRequest();
        enterMatchRequest.setPlayerAttributes(gameOnEnterMatchRequest.getPlayerAttributes());

        final GameOnEnterMatchResponse response = getGameOnMatchInvokerProvider()
            .get()
            .withSession(gameOnSession)
            .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
            .build()
            .postEnterMatch(matchId, enterMatchRequest);

        // Sets the scope of the match to the tournament ID first, so the match will follow the same tournament
        match.setScope(response.getTournamentId());

        // Sets the game on specific metadata to the match
        final Map<String, Serializable> metadata = new HashMap<>();
        final TournamentEntryMetadata tournamentEntryMetadata = new TournamentEntryMetadata();
        tournamentEntryMetadata.setMatchId(response.getMatchId());
        tournamentEntryMetadata.setTournamentId(response.getTournamentId());
        metadata.put(TOURNAMENT_ENTRY_METADATA_KEY, tournamentEntryMetadata);
        match.setMetadata(metadata);

        // Attempts the insert it into the database, assuming that works, we then reply with the match
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

    public Provider<GameOnMatchInvoker.Builder> getGameOnMatchInvokerProvider() {
        return gameOnMatchInvokerProvider;
    }

    @Inject
    public void setGameOnMatchInvokerProvider(Provider<GameOnMatchInvoker.Builder> gameOnMatchInvokerProvider) {
        this.gameOnMatchInvokerProvider = gameOnMatchInvokerProvider;
    }

    public MatchmakingApplicationConfigurationDao getMatchmakingApplicationConfigurationDao() {
        return matchmakingApplicationConfigurationDao;
    }

    @Inject
    public void setMatchmakingApplicationConfigurationDao(MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao) {
        this.matchmakingApplicationConfigurationDao = matchmakingApplicationConfigurationDao;
    }

}
