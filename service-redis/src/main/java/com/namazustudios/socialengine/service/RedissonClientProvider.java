package com.namazustudios.socialengine.service;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.SpotifySrvRedissonClientProvider.REDIS_SRV_PREFIX;
import static org.redisson.config.ReadMode.MASTER;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonClientProvider implements Provider<RedissonClient> {

    public static final String REDIS_URL = "com.namazustudios.socialengine.redis.url";

    public static final String STANDARD = "com.namazustudios.socialengine.redis.provider.standard";

    public static final String SPOTIFY_SRV = "com.namazustudios.socialengine.redis.provider.spotify.srv";

    private Provider<String> redisUrlProvider;

    private Provider<RedissonClient> standard;

    private Provider<RedissonClient> spotifySrv;

    @Override
    public RedissonClient get() {
        if (getRedisUrlProvider().get().startsWith(REDIS_SRV_PREFIX)) {
            return getSpotifySrv().get();
        } else {
            return getStandard().get();
        }
    }

    public Provider<RedissonClient> getStandard() {
        return standard;
    }

    public Provider<String> getRedisUrlProvider() {
        return redisUrlProvider;
    }

    @Inject
    public void setRedisUrlProvider(@Named(RedissonClientProvider.REDIS_URL) Provider<String> redisUrlProvider) {
        this.redisUrlProvider = redisUrlProvider;
    }

    @Inject
    public void setStandard(@Named(STANDARD) Provider<RedissonClient> standard) {
        this.standard = standard;
    }

    public Provider<RedissonClient> getSpotifySrv() {
        return spotifySrv;
    }

    @Inject
    public void setSpotifySrv(@Named(SPOTIFY_SRV) Provider<RedissonClient> spotifySrv) {
        this.spotifySrv = spotifySrv;
    }

}
