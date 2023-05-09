package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.service.Unscoped;
import dev.getelements.elements.service.blockchain.bsc.*;
import dev.getelements.elements.service.blockchain.neo.*;
import dev.getelements.elements.service.blockchain.omni.SuperUserNeoWalletService;

public class DeprecatedServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(NeoWalletService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserNeoWalletService.class);

        bind(BscWalletService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserBscWalletService.class);

        bind(Neow3jClient.class)
                .annotatedWith(Unscoped.class)
                .to(StandardNeow3jClient.class)
                .asEagerSingleton();

        bind(Bscw3jClient.class)
                .annotatedWith(Unscoped.class)
                .to(StandardBscw3jClient.class)
                .asEagerSingleton();

        bind(NeoTokenService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserNeoTokenService.class);

        bind(BscTokenService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserBscTokenService.class);

        bind(Neow3jClient.class).to(StandardNeow3jClient.class).asEagerSingleton();
        bind(Bscw3jClient.class).to(StandardBscw3jClient.class).asEagerSingleton();
    }

}
