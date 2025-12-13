package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.service.facebookiap.client.invoker.FacebookIapReceiptRequestInvoker;
import dev.getelements.elements.service.facebookiap.invoker.DefaultFacebookIapReceiptRequestInvoker;

public class FacebookIapReceiptInvokerModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(FacebookIapReceiptRequestInvoker.class).to(DefaultFacebookIapReceiptRequestInvoker.class);

        expose(FacebookIapReceiptRequestInvoker.class);
    }

}
