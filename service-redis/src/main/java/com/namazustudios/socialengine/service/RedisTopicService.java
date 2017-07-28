package com.namazustudios.socialengine.service;

import org.redisson.Redisson;
import org.redisson.api.RTopic;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedisTopicService implements TopicService {

    private Redisson redisson;

    @Override
    public <T> Topic<T> getTopicForTypeNamed(final Class<T> tClass, final String named) {
            final RTopic<T> rTopic = getRedisson().getTopic(named);
            return null;
        }

    public Redisson getRedisson() {
        return redisson;
    }

    @Inject
    public void setRedisson(Redisson redisson) {
        this.redisson = redisson;
    }

}
