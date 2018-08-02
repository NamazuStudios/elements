package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.GameOnMatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.gameon.GameOnTournamentNotFoundException;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.GameOnTournamentService;
import com.namazustudios.socialengine.service.MatchServiceUtils;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnTournamentInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.EnterTournamentRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.model.gameon.MatchFilter.live;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class UserGameOnTournamentService implements GameOnTournamentService {

    private MatchServiceUtils matchServiceUtils;

    private GameOnMatchDao gameOnMatchDao;

    private MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao;

    private Supplier<Profile> currentProfileSupplier;

    private GameOnSessionService gameOnSessionService;

    private Provider<GameOnMatchInvoker.Builder> gameOnMatchInvokerBuilderProvider;

    private Provider<GameOnTournamentInvoker.Builder> gameOnTournamentInvokerBuilderProvider;

    @Override
    public List<GameOnTournamentSummary> getEligibleTournaments(
            final DeviceOSType deviceOSType, final AppBuildType appBuildType,
            final TournamentFilter filterBy, final TournamentPeriod period, final String playerAttributes) {

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final Set<String> enteredTournamentIdSet = getEnteredMatchIds(gameOnSession, playerAttributes);

        final List<GameOnTournamentSummary> gameOnTournamentSummaries = getGameOnTournamentInvokerBuilderProvider()
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

        final GameOnTournamentDetail gameOnTournamentDetail = getGameOnTournamentInvokerBuilderProvider()
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
            .getSummaries(live, MatchType.developer, TournamentPeriod.all, playerAttributes);

        return gameOnMatchesAggregate
            .getMatches()
            .stream()
            .map(s -> s.getTournamentId())
            .collect(toSet());

    }

    @Override
    public GameOnTournamentEnterResponse enterTournament(
            final String tournamentId,
            final GameOnTournamentEnterRequest gameOnTournamentEnterRequest) {

        final Profile profile = getCurrentProfileSupplier().get();

        final DeviceOSType deviceOSType = gameOnTournamentEnterRequest.getDeviceOSType() == null ?
                                          DeviceOSType.getDefault()                              :
                                          gameOnTournamentEnterRequest.getDeviceOSType();

        final AppBuildType appBuildType = gameOnTournamentEnterRequest.getAppBuildType() == null ?
                                          AppBuildType.getDefault()                              :
                                          gameOnTournamentEnterRequest.getAppBuildType();

        final Match match = gameOnTournamentEnterRequest.getMatch();

        final MatchmakingApplicationConfiguration configuration = getMatchmakingApplicationConfigurationDao()
            .getApplicationConfiguration(profile.getApplication().getId(), match.getScheme());

        if (match.getPlayer() == null) {
            match.setPlayer(profile);
        } else if (!Objects.equals(profile, match.getPlayer())) {
            throw new ForbiddenException("player must match current profile");
        }

        final GameOnSession gameOnSession;
        gameOnSession = getGameOnSessionService().createOrGetCurrentSession(deviceOSType, appBuildType);

        final EnterTournamentRequest enterTournamentRequest;
        enterTournamentRequest = new EnterTournamentRequest();
        enterTournamentRequest.setAccessKey(gameOnTournamentEnterRequest.getAccessKey());
        enterTournamentRequest.setPlayerAttributes(gameOnTournamentEnterRequest.getPlayerAttributes());

        final GameOnTournamentEnterResponse response = getGameOnTournamentInvokerBuilderProvider()
            .get()
            .withSession(gameOnSession)
            .build()
            .postEnterRequest(tournamentId, enterTournamentRequest);

        final Matchmaker matchmaker = getGameOnMatchDao().getMatchmaker(response.getTournamentId());
        final Match inserted = getGameOnMatchDao().createMatch(response.getTournamentId(), match);
        final Match paired = getMatchServiceUtils().attempt(matchmaker, inserted, configuration);
        response.setMatch(paired);

        return response;

    }

    public MatchServiceUtils getMatchServiceUtils() {
        return matchServiceUtils;
    }

    @Inject
    public void setMatchServiceUtils(MatchServiceUtils matchServiceUtils) {
        this.matchServiceUtils = matchServiceUtils;
    }

    public GameOnMatchDao getGameOnMatchDao() {
        return gameOnMatchDao;
    }

    @Inject
    public void setGameOnMatchDao(GameOnMatchDao gameOnMatchDao) {
        this.gameOnMatchDao = gameOnMatchDao;
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

    public Provider<GameOnTournamentInvoker.Builder> getGameOnTournamentInvokerBuilderProvider() {
        return gameOnTournamentInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnTournamentInvokerBuilderProvider(Provider<GameOnTournamentInvoker.Builder> gameOnTournamentInvokerBuilderProvider) {
        this.gameOnTournamentInvokerBuilderProvider = gameOnTournamentInvokerBuilderProvider;
    }



}
