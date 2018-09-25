package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.gameon.GameOnRegistrationNotFoundException;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnEnterMatchRequest;
import com.namazustudios.socialengine.model.gameon.GameOnEnterMatchResponse;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnMatchService;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.MatchServiceUtils;
import com.namazustudios.socialengine.service.ProfileService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.EnterMatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.GameOnConstants.MATCH_METADATA_MATCH_ID;
import static com.namazustudios.socialengine.GameOnConstants.MATCH_METADATA_TOURNAMENT_ID;


public class UserGameOnMatchService implements GameOnMatchService {

    private static final Logger logger = LoggerFactory.getLogger(UserGameOnMatchService.class);

    private MatchDao matchDao;

    private GameOnRegistrationDao gameOnRegistrationDao;

    private MatchServiceUtils matchServiceUtils;

    private ProfileService profileService;

    private GameOnSessionService gameOnSessionService;

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

        final Profile profile = getProfileService().getCurrentProfile();

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
        metadata.put(MATCH_METADATA_MATCH_ID, response.getMatchId());
        metadata.put(MATCH_METADATA_TOURNAMENT_ID, response.getTournamentId());
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

    @Override
    public GameOnGetMatchLeaderboardResponse getLeaderboard(
            final DeviceOSType deviceOSType, final AppBuildType appBuildType,
            final String matchId,
            final Integer currentPlayerNeighbors,
            final Integer limit) {

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final GameOnGetMatchLeaderboardResponse response = getGameOnMatchInvokerProvider()
                .get()
                .withSession(gameOnSession)
                .withExpirationRetry(ex -> getGameOnSessionService().refreshExpiredSession(ex.getExpired()))
                .build()
                .getLeaderboard(matchId, currentPlayerNeighbors, limit);

        fillInProfile(response.getCurrentPlayer());
        fillInProfiles(response.getNeighbors());
        fillInProfiles(response.getLeaderboard());

        // TODO Rework "Next" URL.  We need to know exactly what "next" is, however.

        return response;
    }

    private void fillInProfiles(final List<GameOnGetMatchLeaderboardResponse.LeaderboardItem> itemList) {
        if (itemList == null) return;;
        itemList.forEach(this::fillInProfile);
    }

    private void fillInProfile(GameOnGetMatchLeaderboardResponse.LeaderboardItem item) {
        if (item != null) {

            final String externalPlayerId = item.getExternalPlayerId();

            try {

                final GameOnRegistration gameOnRegistration;
                gameOnRegistration = getGameOnRegistrationDao().getRegistrationForExternalPlayerId(externalPlayerId);

                final Profile profile = gameOnRegistration.getProfile();
                final Profile redacted = getProfileService().redactPrivateInformation(profile);

                item.setProfile(redacted);

            } catch (GameOnRegistrationNotFoundException ex) {
                logger.warn("GameOn Registration not found for player id {}", externalPlayerId);
            }

        }
    }

    public MatchDao getMatchDao() {
        return matchDao;
    }

    @Inject
    public void setMatchDao(MatchDao matchDao) {
        this.matchDao = matchDao;
    }

    public GameOnRegistrationDao getGameOnRegistrationDao() {
        return gameOnRegistrationDao;
    }

    @Inject
    public void setGameOnRegistrationDao(GameOnRegistrationDao gameOnRegistrationDao) {
        this.gameOnRegistrationDao = gameOnRegistrationDao;
    }

    public MatchServiceUtils getMatchServiceUtils() {
        return matchServiceUtils;
    }

    @Inject
    public void setMatchServiceUtils(MatchServiceUtils matchServiceUtils) {
        this.matchServiceUtils = matchServiceUtils;
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    @Inject
    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
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
