package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.invoker.FacebookIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker.OculusIapReceiptRequestInvoker;
import dev.getelements.elements.service.meta.facebookiap.invoker.DefaultFacebookIapReceiptRequestInvoker;
import dev.getelements.elements.service.meta.oculusiap.invoker.DefaultOculusIapReceiptRequestInvoker;

public class MetaIapReceiptInvokerModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(FacebookIapReceiptRequestInvoker.class).to(DefaultFacebookIapReceiptRequestInvoker.class);
        bind(OculusIapReceiptRequestInvoker.class).to(DefaultOculusIapReceiptRequestInvoker.class);

        expose(FacebookIapReceiptRequestInvoker.class);
        expose(OculusIapReceiptRequestInvoker.class);
    }

}
