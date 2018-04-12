package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.service.*;
import org.redisson.api.RListMultimapCache;
import org.redisson.api.RMultimapCache;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.service.RedissonFacebookFriendCache.CACHE_NAME;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedisServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(TopicService.class).to(RedissonTopicService.class).in(ServletScopes.REQUEST);
        bind(FacebookFriendCache.class).to(RedissonFacebookFriendCache.class).in(ServletScopes.REQUEST);
        bind(RedissonClient.class).toProvider(RedissonClientProvider.class).asEagerSingleton();

        bind(new TypeLiteral<RListMultimapCache<String, FacebookFriend>>(){})
            .annotatedWith(named(CACHE_NAME))
            .toProvider(new RedissonObjectProvider<>(r -> {
                final RListMultimapCache<String, FacebookFriend> cache = r.getListMultimapCache(CACHE_NAME);
                cache.expire(1, TimeUnit.SECONDS);
                return cache;
            })).asEagerSingleton();

    }

}
