package com.namazustudios.socialengine.service;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.redisson.config.ReadMode.MASTER;

public class SpotifySrvRedissonClientProvider implements Provider<RedissonClient> {

    public static final String REDIS_SRV_PREFIX = "redis+srv://";

    public static final String SRV_SERVERS = "com.namazustudios.socialengine.redis.srv.servers";

    private Provider<String> srvServers;

    private Provider<String> redisUrlProvider;

    @Override
    public RedissonClient get() {
        return new SpotifySrvRedissonClient.Builder()
            .withQuery(getSrvQuery())
            .withServers(getSrvServers().get())
            .withConfigSupplier(hosts -> {
                final Config config = new Config();
                config.useReplicatedServers().setReadMode(MASTER).addNodeAddress(hosts.toArray(String[]::new));
                return config;
            }) .build();
    }

    private String getSrvQuery() {
        final var url = getRedisUrlProvider().get();
        if (!url.startsWith(REDIS_SRV_PREFIX)) throw new IllegalStateException("Not using redis SRV records.");
        return url.substring(REDIS_SRV_PREFIX.length());
    }

    public Provider<String> getSrvServers() {
        return srvServers;
    }

    @Inject
    public void setSrvServers(@Named(SRV_SERVERS) Provider<String> srvServers) {
        this.srvServers = srvServers;
    }

    public Provider<String> getRedisUrlProvider() {
        return redisUrlProvider;
    }

    @Inject
    public void setRedisUrlProvider(@Named(RedissonClientProvider.REDIS_URL) Provider<String> redisUrlProvider) {
        this.redisUrlProvider = redisUrlProvider;
    }

}
