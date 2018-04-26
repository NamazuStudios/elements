package com.namazustudios.socialengine.service;

import org.redisson.api.RedissonClient;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonTopicService implements TopicService {

    private RedissonClient redisson;

    @Override
    public <T> Topic<T> getTopicForTypeNamed(final Class<T> tClass, final String named) {
        return new RedissonTopic<>(getRedisson(), tClass, named);
    }

    public RedissonClient getRedisson() {
        return redisson;
    }

    @Inject
    public void setRedisson(RedissonClient redisson) {
        this.redisson = redisson;
    }

}
