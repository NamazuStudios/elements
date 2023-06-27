package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.security.PasswordGenerator;
import dev.getelements.elements.security.SecureRandomPasswordGenerator;
import dev.getelements.elements.service.MatchServiceUtils;
import dev.getelements.elements.service.ServicesDozerMapperProvider;
import dev.getelements.elements.service.blockchain.crypto.*;
import dev.getelements.elements.service.match.StandardMatchServiceUtils;
import dev.getelements.elements.service.util.CipherUtility;
import dev.getelements.elements.service.util.CryptoKeyPairUtility;
import dev.getelements.elements.service.util.StandardCipherUtility;
import dev.getelements.elements.service.util.StandardCryptoKeyPairUtility;
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
