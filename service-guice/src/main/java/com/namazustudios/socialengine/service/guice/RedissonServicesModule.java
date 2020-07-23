package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.service.*;
import org.redisson.api.RListMultimapCache;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonServicesModule extends AbstractModule {

    private final Scope scope;

    public RedissonServicesModule(final Scope scope) {
        this.scope = scope;
    }

    @Override
    protected void configure() {

        bind(TopicService.class).to(RedissonTopicService.class).in(scope);
        bind(FacebookFriendCache.class).to(RedissonFacebookFriendCache.class).in(scope);
        bind(RedissonClient.class).toProvider(RedissonClientProvider.class).asEagerSingleton();

        bind(new TypeLiteral<RListMultimapCache<String, FacebookFriend>>(){})
            .annotatedWith(named(RedissonFacebookFriendCache.CACHE_NAME))
            .toProvider(new RedissonObjectProvider<>(r -> {
                final RListMultimapCache<String, FacebookFriend> cache = r.getListMultimapCache(RedissonFacebookFriendCache.CACHE_NAME);
                cache.expire(1, TimeUnit.MINUTES);
                return cache;
            })).asEagerSingleton();

    }

}
