package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.service.RedissonClientProvider;
import com.namazustudios.socialengine.service.RedissonTopicService;
import com.namazustudios.socialengine.service.TopicService;
import org.redisson.api.RedissonClient;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedisServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TopicService.class).to(RedissonTopicService.class).in(ServletScopes.REQUEST);
        bind(RedissonClient.class).toProvider(RedissonClientProvider.class).asEagerSingleton();
    }

}
