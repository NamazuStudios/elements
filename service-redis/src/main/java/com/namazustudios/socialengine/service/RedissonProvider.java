package com.namazustudios.socialengine.service;

import org.redisson.Redisson;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonProvider implements Provider<Redisson> {

    @Override
    public Redisson get() {
        return null;
    }

}
