package dev.getelements.elements.sdk.service.topic;

/**
 * Entry point for manging {@link Topic} instances in the system.  {@link Topic}s are
 * a means for the system, as a whole, to publish asynchronous messages.
 *
 * Created by patricktwohig on 7/20/17.
 */
public interface TopicService {

    /**
     * Gets a {@link Topic} for the supplied message type.
     *
     * @param tClass the message type {@link Class<T>} instance
     * @param named the name of the topic
     * @param <T> the message type
     * @return a {@link Topic}
     */
    <T> Topic<T> getTopicForTypeNamed(Class<T> tClass, String named);

}
