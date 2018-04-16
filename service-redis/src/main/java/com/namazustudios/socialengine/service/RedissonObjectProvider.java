package com.namazustudios.socialengine.service;

import org.redisson.Redisson;
import org.redisson.api.RObject;
import org.redisson.api.RedissonClient;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Function;

public class RedissonObjectProvider<RObjectT extends RObject> implements Provider<RObjectT> {

    private Provider<RedissonClient> redissonClientProvider;

    private final Function<RedissonClient, RObjectT> providerFunction;

    public RedissonObjectProvider(Function<RedissonClient, RObjectT> providerFunction) {
        this.providerFunction = providerFunction;
    }

    @Override
    public RObjectT get() {
        final RedissonClient redissonClient = getRedissonClientProvider().get();
        return providerFunction.apply(redissonClient);
    }

    public Provider<RedissonClient> getRedissonClientProvider() {
        return redissonClientProvider;
    }

    @Inject
    public void setRedissonClientProvider(Provider<RedissonClient> redissonClientProvider) {
        this.redissonClientProvider = redissonClientProvider;
    }

}
