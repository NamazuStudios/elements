package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.service.blockchain.crypto.*;

public class OmniBlockchainServicesUtilityModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(VaultCryptoUtilities.class)
                .to(AesVaultCryptoUtilities.class)
                .asEagerSingleton();

        bind(WalletCryptoUtilities.class)
                .to(StandardWalletCryptoUtilities.class)
                .asEagerSingleton();

        bind(WalletAccountFactory.class)
                .to(StandardWalletAccountFactory.class)
                .asEagerSingleton();

    }

}
