package com.namazustudios.socialengine.service;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ElasticacheServersConfig;
import org.redisson.config.ReadMode;
import org.redisson.config.ReplicatedServersConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.redisson.config.ReadMode.MASTER;
import static org.redisson.config.ReadMode.MASTER_SLAVE;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonClientProvider implements Provider<RedissonClient> {

    public static final String REDIS_URL = "com.namazustudios.socialengine.redis.url";

    private String redisUrl;

    @Override
    public RedissonClient get() {

        // TODO Add support for externalized redisson configuration file as
        // part of the main social engine configuration.  For now, this is a
        // simple solution which lets us get up and running with Redis.

        final Config config = new Config();

        config.useReplicatedServers()
              .setReadMode(MASTER)
              .addNodeAddress(getRedisUrl());

        return Redisson.create(config);

    }

    public String getRedisUrl() {
        return redisUrl;
    }

    @Inject
    public void setRedisUrl(@Named(REDIS_URL) String redisUrl) {
        this.redisUrl = redisUrl;
    }

}
