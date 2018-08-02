package com.namazustudios.socialengine.service.match;

import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.MatchmakingApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.MatchServiceUtils;
import com.namazustudios.socialengine.service.MatchService;
import com.namazustudios.socialengine.service.Topic;
import com.namazustudios.socialengine.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 7/20/17.
 */
public class UserMatchService implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(UserMatchService.class);

    private Supplier<Profile> currentProfileSupplier;

    private MatchDao matchDao;

    private TopicService topicService;

    private MatchServiceUtils matchServiceUtils;

    private MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao;

    @Override
    public Match getMatch(final String matchId) {
        final Profile profile = getCurrentProfileSupplier().get();
        return getMatchServiceUtils().redactOpponentUser(getMatchDao().getMatchForPlayer(profile.getId(), matchId));
    }

    @Override
    public Pagination<Match> getMatches(final int offset, final int count) {
        final Profile profile = getCurrentProfileSupplier().get();
        return getMatchDao()
            .getMatchesForPlayer(profile.getId(), offset, count)
            .transform(getMatchServiceUtils()::redactOpponentUser);
    }

    @Override
    public Pagination<Match> getMatches(final int offset, final int count, final String search) {
        final Profile profile = getCurrentProfileSupplier().get();
        return getMatchDao()
            .getMatchesForPlayer(profile.getId(), offset, count, search)
            .transform(getMatchServiceUtils()::redactOpponentUser);
    }

    @Override
    public Match createMatch(final Match match) {

        final Profile profile = getCurrentProfileSupplier().get();

        if (match.getPlayer() == null) {
            match.setPlayer(profile);
        } else if (!Objects.equals(profile, match.getPlayer())) {
            throw new ForbiddenException("player must match current profile");
        } else if (match.getOpponent() != null) {
            throw new InvalidDataException("must not specify opponent when creating a match.");
        }

        final Match newMatch = getMatchDao().createMatch(match);
        return attempt(newMatch);

    }

    @Override
    public Topic.Subscription attemptRematchAndPoll(
            final String matchId,
            final Consumer<Match> matchConsumer, final Consumer<Exception> exceptionConsumer) {

        final Profile profile = getCurrentProfileSupplier().get();
        final Match match = getMatchDao().getMatchForPlayer(profile.getId(), matchId);

        final Topic.Subscription subscription =  getTopicService()
            .getTopicForTypeNamed(Match.class, Match.ROOT_TOPIC)
            .getSubtopicNamed(match.getId())
            .subscribeNext(m -> matchConsumer.accept(getMatchServiceUtils().redactOpponentUser(m)), exceptionConsumer);

        try {
            attempt(match);
        } catch (NoSuitableMatchException ex) {
            logger.debug("No match found in polling process.  Skipping.", ex);
        }

        return subscription;

    }

    private Match attempt(final Match match) throws NoSuitableMatchException {

        final Profile profile = getCurrentProfileSupplier().get();

        final MatchmakingApplicationConfiguration configuration = getMatchmakingApplicationConfigurationDao()
            .getApplicationConfiguration(profile.getApplication().getId(), match.getScheme());

        final Matchmaker matchmaker = getMatchDao().getMatchmaker(configuration.getAlgorithm());
        return getMatchServiceUtils().attempt(matchmaker, match, configuration);

    }

    @Override
    public void deleteMatch(final String matchId) {

        final Profile profile = getCurrentProfileSupplier().get();
        getMatchDao().deleteMatch(profile.getId(), matchId);

        final Topic<Exception> exceptionTopic = getTopicService()
            .getTopicForTypeNamed(Exception.class, Match.ROOT_TOPIC)
            .getSubtopicNamed(profile.getId())
            .getSubtopicNamed(matchId);

        try (final Topic.Publisher<Exception> exceptionPublisher = exceptionTopic.getPublisher()) {
            exceptionPublisher.accept(null);
        }

    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    public MatchDao getMatchDao() {
        return matchDao;
    }

    @Inject
    public void setMatchDao(MatchDao matchDao) {
        this.matchDao = matchDao;
    }

    public TopicService getTopicService() {
        return topicService;
    }

    @Inject
    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
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

}
