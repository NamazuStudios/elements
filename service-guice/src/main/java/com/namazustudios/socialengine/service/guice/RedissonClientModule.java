package com.namazustudios.socialengine.service.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.RedissonClientProvider;
import com.namazustudios.socialengine.service.SpotifySrvRedissonClientProvider;
import com.namazustudios.socialengine.service.StandardRedissionClientProvider;
import org.redisson.api.RedissonClient;

import static com.google.inject.name.Names.named;

public class RedissonClientModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(RedissonClient.class).toProvider(RedissonClientProvider.class).asEagerSingleton();

        bind(RedissonClient.class)
            .annotatedWith(named(RedissonClientProvider.STANDARD))
            .toProvider(StandardRedissionClientProvider.class);

        bind(RedissonClient.class)
            .annotatedWith(named(RedissonClientProvider.SPOTIFY_SRV))
            .toProvider(SpotifySrvRedissonClientProvider.class);

        expose(RedissonClient.class);

    }
}
