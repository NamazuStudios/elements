package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;

public class AppleIapReceiptInvokerModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(AppleIapVerifyReceiptInvoker.Builder.class);
        bind(AppleIapVerifyReceiptInvoker.Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);
    }

}
