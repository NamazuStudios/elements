package com.namazustudios.socialengine.service.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;

public class AppleIapReceiptInvokerModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(AppleIapVerifyReceiptInvoker.Builder.class);
        bind(AppleIapVerifyReceiptInvoker.Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);
    }

}
