package com.namazustudios.socialengine.service.match;

import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.TimeDelta;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.MatchService;
import com.namazustudios.socialengine.service.Topic;
import com.namazustudios.socialengine.service.TopicService;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 7/20/17.
 */
public class UserMatchService implements MatchService {

    private Supplier<Profile> currentProfileSupplier;

    private MatchDao matchDao;

    private TopicService topicService;

    @Override
    public Match getMatch(final String matchId) {
        final Profile profile = getCurrentProfileSupplier().get();
        return redactOpponentUser(getMatchDao().getMatchForPlayer(profile.getId(), matchId));
    }

    @Override
    public Pagination<Match> getMatches(final int offset, final int count) {
        final Profile profile = getCurrentProfileSupplier().get();
        return getMatchDao()
            .getMatchesForPlayer(profile.getId(), offset, count)
            .transform(this::redactOpponentUser);
    }

    @Override
    public Pagination<Match> getMatches(final int offset, final int count, final String search) {
        final Profile profile = getCurrentProfileSupplier().get();
        return getMatchDao()
            .getMatchesForPlayer(profile.getId(), offset, count, search)
            .transform(this::redactOpponentUser);
    }

    @Override
    public Match createMatch(final Match match) {

        final Profile profile = getCurrentProfileSupplier().get();

        if (match.getPlayer() == null) {
            match.setOpponent(profile);
        } else if (!Objects.equals(profile, match.getPlayer())) {
            throw new ForbiddenException("player must match current profile");
        }

        final MatchDao.TimeDeltaTuple tuple = getMatchDao().createMatchAndLogDelta(match);

        final Topic<MatchTimeDelta> matchTimeDeltaTopic;

        matchTimeDeltaTopic = getTopicService()
            .getTopicForTypeNamed(MatchTimeDelta.class, MatchTimeDelta.ROOT_DELTA_TOPIC)
            .getSubtopicNamed(profile.getId())
            .getSubtopicNamed(tuple.getTimeDelta().getId());

        try (final Topic.Publisher<MatchTimeDelta> matchTimeDeltaPublisher = matchTimeDeltaTopic.getPublisher()) {
            matchTimeDeltaPublisher.accept(tuple.getTimeDelta());
        }

        return redactOpponentUser(tuple.getMatch());

    }

    @Override
    public void deleteMatch(final String matchId) {

        final Profile profile = getCurrentProfileSupplier().get();
        final MatchTimeDelta matchTimeDelta = getMatchDao().deleteMatchAndLogDelta(profile.getId(), matchId);

        final Topic<MatchTimeDelta> matchTimeDeltaTopic;

        matchTimeDeltaTopic = getTopicService()
            .getTopicForTypeNamed(MatchTimeDelta.class, MatchTimeDelta.ROOT_DELTA_TOPIC)
            .getSubtopicNamed(profile.getId())
            .getSubtopicNamed(matchTimeDelta.getId());

        try (final Topic.Publisher<MatchTimeDelta> matchTimeDeltaPublisher = matchTimeDeltaTopic.getPublisher()) {
            matchTimeDeltaPublisher.accept(matchTimeDelta);
        }

    }

    @Override
    public List<TimeDelta<String, Match>> getDeltas(final long timeStamp) {

        final Profile profile = getCurrentProfileSupplier().get();

        return getMatchDao()
            .getDeltasForPlayerAfter(profile.getId(), timeStamp)
            .stream()
            .map(this::redactOpponentUser)
            .collect(Collectors.toList());

    }

    @Override
    public List<TimeDelta<String, Match>> getDeltasForMatch(final  long timeStamp, final String matchId) {

        final Profile profile = getCurrentProfileSupplier().get();

        return getMatchDao()
            .getDeltasForPlayerAfter(profile.getId(), timeStamp, matchId)
            .stream()
            .map(this::redactOpponentUser)
            .collect(Collectors.toList());

    }

    @Override
    public Topic.Subscription waitForDeltas(
            final long timeStamp,
            final Consumer<List<MatchTimeDelta>> timeDeltaListConsumer,
            final Consumer<Exception> exceptionConsumer) {

        final Profile profile = getCurrentProfileSupplier().get();

        final Topic<MatchTimeDelta> matchTimeDeltaTopic;
        matchTimeDeltaTopic = getTopicService()
                .getTopicForTypeNamed(MatchTimeDelta.class, MatchTimeDelta.ROOT_DELTA_TOPIC)
                .getSubtopicNamed(profile.getId());

        return matchTimeDeltaTopic.subscribeNext(matchTimeDelta -> {
            if (matchTimeDelta.getTimeStamp() >= timeStamp) {
                final List<MatchTimeDelta> matchTimeDeltaList;
                matchTimeDeltaList = getMatchDao().getDeltasForPlayerAfter(profile.getId(), timeStamp);
                timeDeltaListConsumer.accept(matchTimeDeltaList);
            }
        }, exceptionConsumer);

    }

    @Override
    public Topic.Subscription waitForDeltas(
            final long timeStamp,
            final String matchId,
            final Consumer<List<MatchTimeDelta>> timeDeltaListConsumer,
            final Consumer<Exception> exceptionConsumer) {

        final Profile profile = getCurrentProfileSupplier().get();
        final Match match = getMatchDao().getMatchForPlayer(profile.getId(), matchId);

        final Topic<MatchTimeDelta> matchTimeDeltaTopic;
        matchTimeDeltaTopic = getTopicService()
                .getTopicForTypeNamed(MatchTimeDelta.class, MatchTimeDelta.ROOT_DELTA_TOPIC)
                .getSubtopicNamed(profile.getId())
                .getSubtopicNamed(match.getId());

        return matchTimeDeltaTopic.subscribeNext(matchTimeDelta ->  {
            if (matchTimeDelta.getTimeStamp() >= timeStamp) {
                final List<MatchTimeDelta> matchTimeDeltaList;
                matchTimeDeltaList = getMatchDao().getDeltasForPlayerAfter(profile.getId(), timeStamp, matchId);
                timeDeltaListConsumer.accept(matchTimeDeltaList);
            }
        }, exceptionConsumer);

    }

    private Match redactOpponentUser(final Match match) {
        match.getOpponent().setUser(null);
        return match;
    }

    private TimeDelta<String, Match> redactOpponentUser(final TimeDelta<String, Match> stringMatchTimeDelta) {

        final Match match = stringMatchTimeDelta.getSnapshot();

        if (match != null) {
            stringMatchTimeDelta.setSnapshot(redactOpponentUser(match));
        }

        return stringMatchTimeDelta;

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

}
