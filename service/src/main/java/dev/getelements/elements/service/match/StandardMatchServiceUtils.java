package dev.getelements.elements.service.match;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.Matchmaker;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.NoSuitableMatchException;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.query.ElementQuery;
import dev.getelements.elements.sdk.service.match.MatchServiceUtils;
import dev.getelements.elements.sdk.service.topic.Topic;
import dev.getelements.elements.sdk.service.topic.TopicService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.sdk.service.match.MatchService.EVENT_1V1_MADE;

public class StandardMatchServiceUtils implements MatchServiceUtils {

    private static final Logger logger = LoggerFactory.getLogger(StandardMatchServiceUtils.class);

    private TopicService topicService;

    private ElementRegistry elementRegistry;

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

        final var callbackDefinition = matchmakingApplicationConfiguration.getSuccess();
        final var elementServiceReference = callbackDefinition.getService();

        final var callback = new ElementQuery(getElementRegistry(), elementServiceReference.getElementName(),0)
                .service(elementServiceReference.getServiceName())
                .callback(callbackDefinition.getMethod(), Profile.class, Profile.class)
                .get()
                .as(String.class);

        final var result = callback.call(player, opponent);

        final var event = new Event.Builder()
                .named(EVENT_1V1_MADE)
                .argument(player)
                .argument(opponent)
                .build();

        getElementRegistry().publish(event);

        return result;

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

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

    public TopicService getTopicService() {
        return topicService;
    }

    @Inject
    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
    }

}
