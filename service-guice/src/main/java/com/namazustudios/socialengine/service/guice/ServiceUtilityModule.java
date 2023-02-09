package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.security.SecureRandomPasswordGenerator;
import com.namazustudios.socialengine.service.MatchServiceUtils;
import com.namazustudios.socialengine.service.ServicesDozerMapperProvider;
import com.namazustudios.socialengine.service.blockchain.crypto.*;
import com.namazustudios.socialengine.service.match.StandardMatchServiceUtils;
import com.namazustudios.socialengine.service.util.CipherUtility;
import com.namazustudios.socialengine.service.util.CryptoKeyPairUtility;
import com.namazustudios.socialengine.service.util.StandardCipherUtility;
import com.namazustudios.socialengine.service.util.StandardCryptoKeyPairUtility;
import org.dozer.Mapper;

public class ServiceUtilityModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(CipherUtility.class)
                .to(StandardCipherUtility.class)
                .asEagerSingleton();

        bind(CryptoKeyPairUtility.class)
                .to(StandardCryptoKeyPairUtility.class)
                .asEagerSingleton();

        bind(Mapper.class)
                .toProvider(ServicesDozerMapperProvider.class)
                .asEagerSingleton();

        bind(MatchServiceUtils.class)
                .to(StandardMatchServiceUtils.class);

        bind(PasswordGenerator.class)
                .to(SecureRandomPasswordGenerator.class)
                .asEagerSingleton();

    }
}
