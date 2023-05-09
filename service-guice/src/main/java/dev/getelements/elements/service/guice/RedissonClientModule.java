package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.service.RedissonClientProvider;
import dev.getelements.elements.service.SpotifySrvRedissonClientProvider;
import dev.getelements.elements.service.StandardRedissionClientProvider;
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
