package dev.getelements.elements.service;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.redisson.config.ReadMode.MASTER;

public class StandardRedissionClientProvider implements Provider<RedissonClient> {

    private Provider<String> redisUrlProvider;

    @Override
    public RedissonClient get() {

        final Config config = new Config();

        config.useReplicatedServers()
            .setReadMode(MASTER)
            .addNodeAddress(getRedisUrlProvider().get());

        return Redisson.create(config);

    }

    public Provider<String> getRedisUrlProvider() {
        return redisUrlProvider;
    }

    @Inject
    public void setRedisUrlProvider(@Named(RedissonClientProvider.REDIS_URL) Provider<String> redisUrlProvider) {
        this.redisUrlProvider = redisUrlProvider;
    }

}
