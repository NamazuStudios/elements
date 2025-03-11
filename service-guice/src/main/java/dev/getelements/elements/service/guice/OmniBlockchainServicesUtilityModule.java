package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.service.blockchain.crypto.StandardWalletCryptoUtilities;
import dev.getelements.elements.sdk.service.blockchain.crypto.VaultCryptoUtilities;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletAccountFactory;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletCryptoUtilities;
import dev.getelements.elements.service.blockchain.crypto.AesVaultCryptoUtilities;
import dev.getelements.elements.service.blockchain.crypto.StandardWalletAccountFactory;

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
