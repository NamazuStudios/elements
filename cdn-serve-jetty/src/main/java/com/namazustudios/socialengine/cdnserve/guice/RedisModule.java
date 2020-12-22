package com.namazustudios.socialengine.cdnserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.service.RedissonClientProvider;
import com.namazustudios.socialengine.service.RedissonTopicService;
import com.namazustudios.socialengine.service.TopicService;
import org.redisson.api.RedissonClient;

/**
 * Created by garrettmcspadden on 12/21/20.
 */
public class RedisModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TopicService.class).to(RedissonTopicService.class).in(ServletScopes.REQUEST);
        bind(RedissonClient.class).toProvider(RedissonClientProvider.class).asEagerSingleton();
    }

}
