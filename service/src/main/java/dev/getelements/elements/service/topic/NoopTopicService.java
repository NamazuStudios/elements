package dev.getelements.elements.service.topic;

import dev.getelements.elements.sdk.service.topic.Topic;
import dev.getelements.elements.sdk.service.topic.TopicService;

public class NoopTopicService implements TopicService {

    @Override
    public <T> Topic<T> getTopicForTypeNamed(final Class<T> tClass, final String named) {
        throw new UnsupportedOperationException("Not currently supported.");
    }

}
