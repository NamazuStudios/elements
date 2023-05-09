package dev.getelements.elements.codeserve;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.service.RedissonClientProvider;
import dev.getelements.elements.service.RedissonTopicService;
import dev.getelements.elements.service.TopicService;
import org.redisson.api.RedissonClient;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class RedisModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TopicService.class).to(RedissonTopicService.class).in(ServletScopes.REQUEST);
        bind(RedissonClient.class).toProvider(RedissonClientProvider.class).asEagerSingleton();
    }

}
