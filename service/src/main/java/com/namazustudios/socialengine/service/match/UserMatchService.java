package com.namazustudios.socialengine.service.match;

import com.namazustudios.socialengine.dao.ContextFactory;
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
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.service.MatchService;
import com.namazustudios.socialengine.service.Topic;
import com.namazustudios.socialengine.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 7/20/17.
 */
public class UserMatchService implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(UserMatchService.class);

    private Provider<Attributes> attributesProvider;

    private Supplier<Profile> currentProfileSupplier;

    private MatchDao matchDao;

    private TopicService topicService;

    private ContextFactory contextFactory;

    private MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao;

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

        final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration;
        matchmakingApplicationConfiguration = getMatchmakingApplicationConfigurationDao()
            .getApplicationConfiguration(profile.getApplication().getId(), match.getScheme());

        if (match.getPlayer() == null) {
            match.setPlayer(profile);
        } else if (!Objects.equals(profile, match.getPlayer())) {
            throw new ForbiddenException("player must match current profile");
        } else if (match.getOpponent() != null) {
            throw new InvalidDataException("must not specifcy opponent when creating a match.");
        }

        final Match newMatch = getMatchDao().createMatch(match);

        try {

            final Matchmaker matchmaker = getMatchDao().getMatchmaker(matchmakingApplicationConfiguration.getAlgorithm());

            final Matchmaker.SuccessfulMatchTuple successfulMatchTuple = matchmaker
                .attemptToFindOpponent(newMatch, (p, o) -> finalize(p, o, matchmakingApplicationConfiguration));

            return handleSuccessfulMatch(successfulMatchTuple);

        } catch (NoSuitableMatchException ex) {
            return redactOpponentUser(newMatch);
        }

    }

    private Match handleSuccessfulMatch(final Matchmaker.SuccessfulMatchTuple successfulMatchTuple) {
        notifyComplete(successfulMatchTuple.getPlayerMatch());
        notifyComplete(successfulMatchTuple.getOpponentMatch());
        return redactOpponentUser(successfulMatchTuple.getPlayerMatch());
    }

    private void notifyComplete(final Match match) {

        final Topic<Match> matchTopic = getTopicService()
                .getTopicForTypeNamed(Match.class, Match.ROOT_TOPIC)
                .getSubtopicNamed(match.getId());

        try (final Topic.Publisher<Match> matchPublisher = matchTopic.getPublisher()) {
            matchPublisher.accept(match);
        }

    }

    private String finalize(final Match player, final Match opponent,
                            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {

        final String module = matchmakingApplicationConfiguration.getSuccess().getModule();
        final String method = matchmakingApplicationConfiguration.getSuccess().getMethod();

        final Profile profile = getCurrentProfileSupplier().get();
        final Context context = getContextFactory().getContextForApplication(profile.getApplication().getId());

        final Attributes attributes = new SimpleAttributes.Builder()
            .from(getAttributesProvider().get(), (n, v) -> v instanceof Serializable)
            .build();

        final Object result = context
            .getHandlerContext()
            .invokeRetainedHandler(attributes, module, method, player, opponent);

        if (!(result instanceof String)) {
            throw new InternalError("Returned value not string from match processor.");
        }

        return (String) result;

    }

    @Override
    public Topic.Subscription waitForUpdate(
            final String matchId, final long timeStamp,
            final Consumer<Match> matchConsumer, final Consumer<Exception> exceptionConsumer) {

        final Profile profile = getCurrentProfileSupplier().get();
        final Match match = getMatchDao().getMatchForPlayer(profile.getId(), matchId);

        return getTopicService().getTopicForTypeNamed(Match.class, Match.ROOT_TOPIC)
            .getSubtopicNamed(match.getId())
            .subscribeNext(m -> matchConsumer.accept(redactOpponentUser(m)), exceptionConsumer);

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

    private Match redactOpponentUser(final Match match) {

        final Profile opponent = match.getOpponent();

        if (opponent != null) {
            match.getOpponent().setUser(null);
        }

        return match;

    }

    public Provider<Attributes> getAttributesProvider() {
        return attributesProvider;
    }

    @Inject
    public void setAttributesProvider(Provider<Attributes> attributesProvider) {
        this.attributesProvider = attributesProvider;
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

    public ContextFactory getContextFactory() {
        return contextFactory;
    }

    @Inject
    public void setContextFactory(ContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public MatchmakingApplicationConfigurationDao getMatchmakingApplicationConfigurationDao() {
        return matchmakingApplicationConfigurationDao;
    }

    @Inject
    public void setMatchmakingApplicationConfigurationDao(MatchmakingApplicationConfigurationDao matchmakingApplicationConfigurationDao) {
        this.matchmakingApplicationConfigurationDao = matchmakingApplicationConfigurationDao;
    }

}
