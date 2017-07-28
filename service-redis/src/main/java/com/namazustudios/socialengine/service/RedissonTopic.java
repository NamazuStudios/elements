package com.namazustudios.socialengine.service;

import com.google.common.base.Joiner;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.PatternMessageListener;

import java.util.function.Consumer;

import static com.namazustudios.socialengine.service.Topic.checkValidName;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonTopic<T> implements Topic<T> {

    private static final String WILDCARD = "*";

    private static final String SUBTOPIC_SEPARATOR = ":";

    private final String name;

    private final RedissonClient redisson;

    public RedissonTopic(final RedissonClient redisson, final String name) {
        this.redisson = redisson;
        this.name = checkValidName(name);
    }

    @Override
    public Subscription subscribe(final Consumer<T> consumer) {
        final RPatternTopic<T> rTopic = redisson.getPatternTopic(name + WILDCARD);
        final PatternMessageListener<T> patternMessageListener = (pattern, channel, msg) -> consumer.accept(msg);
        rTopic.addListener(patternMessageListener);
        return () -> rTopic.removeListener(patternMessageListener);
    }

    @Override
    public Publisher<T> getPublisher() {
        final RTopic<T> rTopic = redisson.getTopic(name);
        return t -> rTopic.publish(t);
    }

    @Override
    public Topic<T> getSubtopicNamed(String name) {
        final String fullName = Joiner.on(SUBTOPIC_SEPARATOR).join(this.name, checkValidName(name));
        return new RedissonTopic<T>(redisson, fullName);
    }

    @Override
    public String toString() {
        return "RedissonTopic{" +
                "name='" + name + '\'' +
                '}';
    }

}
