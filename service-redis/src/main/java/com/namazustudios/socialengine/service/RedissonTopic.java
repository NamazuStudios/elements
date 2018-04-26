package com.namazustudios.socialengine.service;

import com.google.common.base.Joiner;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.PatternMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static com.namazustudios.socialengine.service.Topic.checkValidName;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonTopic<T> implements Topic<T> {

    private static final String WILDCARD = "*";

    private static final String SEPARATOR = ":";

    private Logger logger;

    private final String name;

    private final Class<T> tClass;

    private final RedissonClient redisson;

    public RedissonTopic(final RedissonClient redisson, final Class<T> tClass, final String name) {
        this.tClass = tClass;
        this.redisson = redisson;
        this.name = checkValidName(name);
    }

    public RedissonTopic(final RedissonClient redisson,  final Class<T> tClass, final RedissonTopic<T> parent, final String name) {
        this.tClass = tClass;
        this.redisson = redisson;
        this.name = Joiner.on(SEPARATOR).join(parent.name, name);
    }

    @Override
    public Subscription subscribe(final Consumer<T> consumer) {

        final RPatternTopic<T> rTopic = redisson.getPatternTopic(name + WILDCARD);
        final PatternMessageListener<T> patternMessageListener = (pattern, channel, msg) -> {
            if (msg == null || tClass.isInstance(msg)) {
                consumer.accept(msg);
            } else {
                getLogger().debug("Got unexpected type {}.  Expecting {}", msg.getClass(), tClass.getSimpleName());
            }
        };

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
        return new RedissonTopic<T>(redisson, tClass, this, name);
    }

    @Override
    public String toString() {
        return "RedissonTopic{ name='" + name + '\'' + '}';
    }

    private Logger getLogger() {
        return logger != null ? logger : (logger = LoggerFactory.getLogger(RedissonTopic.class.getName() + "." + name));
    }

}
