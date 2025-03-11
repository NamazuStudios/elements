package dev.getelements.elements.service.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.model.security.PasswordGenerator;
import dev.getelements.elements.sdk.service.match.MatchServiceUtils;
import dev.getelements.elements.sdk.service.topic.TopicService;
import dev.getelements.elements.sdk.service.util.*;
import dev.getelements.elements.security.SecureRandomPasswordGenerator;
import dev.getelements.elements.service.match.StandardMatchServiceUtils;
import dev.getelements.elements.service.topic.NoopTopicService;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import dev.getelements.elements.service.util.StandardCipherUtility;
import dev.getelements.elements.service.util.StandardCryptoKeyPairUtility;

public class ServiceUtilityModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(ObjectMapper.class)
                .asEagerSingleton();

        bind(CipherUtility.class)
                .to(StandardCipherUtility.class)
                .asEagerSingleton();

        bind(CryptoKeyPairUtility.class)
                .to(StandardCryptoKeyPairUtility.class)
                .asEagerSingleton();

        bind(MapperRegistry.class)
                .toProvider(ServicesMapperRegistryProvider.class)
                .asEagerSingleton();

        bind(MatchServiceUtils.class)
                .to(StandardMatchServiceUtils.class);

        bind(PasswordGenerator.class)
                .to(SecureRandomPasswordGenerator.class)
                .asEagerSingleton();

        bind(TopicService.class)
                .to(NoopTopicService.class)
                .asEagerSingleton();

    }
}
