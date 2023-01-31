package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.blockchain.crypto.*;

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
