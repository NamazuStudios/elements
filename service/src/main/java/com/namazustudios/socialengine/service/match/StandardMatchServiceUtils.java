package com.namazustudios.socialengine.service.match;

import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.CallbackDefinition;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.service.MatchServiceUtils;
import com.namazustudios.socialengine.service.Topic;
import com.namazustudios.socialengine.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;

public class StandardMatchServiceUtils implements MatchServiceUtils {

    private static final Logger logger = LoggerFactory.getLogger(StandardMatchServiceUtils.class);

    private TopicService topicService;

    private ContextFactory contextFactory;

    private Provider<Attributes> attributesProvider;

    @Override
    public Match attempt(final Matchmaker matchmaker, final Match match,
                         final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {

        try {

            final Matchmaker.SuccessfulMatchTuple successfulMatchTuple = matchmaker
                .attemptToFindOpponent(match, (p, o) -> finalize(p, o, matchmakingApplicationConfiguration));

            return handleSuccessfulMatch(successfulMatchTuple);

        } catch (NoSuitableMatchException ex) {
            return redactOpponentUser(match);
        }

    }

    private String finalize(final Match player, final Match opponent,
                            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {

        final Application application = matchmakingApplicationConfiguration.getParent();
        final Context context = getContextFactory().getContextForApplication(application.getId());

        final CallbackDefinition success = matchmakingApplicationConfiguration.getSuccess();
        final String module = success.getModule();
        final String method = success.getMethod();

        final Attributes attributes = new SimpleAttributes.Builder()
            .from(getAttributesProvider().get(), (n, v) -> v instanceof Serializable)
            .build();

        final Object result = context
            .getHandlerContext()
            .invokeRetainedHandler(attributes, module, method, player, opponent);

        logger.debug("Player {} Opponent {}", player, opponent);

        if (!(result instanceof String)) {
            throw new InternalError("Returned value not string from match processor.");
        }

        return (String) result;

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

    @Override
    public Match redactOpponentUser(final Match match) {

        final Profile opponent = match.getOpponent();

        if (opponent != null) {
            match.getOpponent().setUser(null);
        }

        return match;

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

    public Provider<Attributes> getAttributesProvider() {
        return attributesProvider;
    }

    @Inject
    public void setAttributesProvider(Provider<Attributes> attributesProvider) {
        this.attributesProvider = attributesProvider;
    }

}
