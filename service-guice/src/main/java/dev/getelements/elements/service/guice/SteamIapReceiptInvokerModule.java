package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.service.steam.invoker.DefaultSteamIapReceiptRequestInvoker;

public class SteamIapReceiptInvokerModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(SteamIapReceiptRequestInvoker.class).to(DefaultSteamIapReceiptRequestInvoker.class);
        expose(SteamIapReceiptRequestInvoker.class);
    }

}
