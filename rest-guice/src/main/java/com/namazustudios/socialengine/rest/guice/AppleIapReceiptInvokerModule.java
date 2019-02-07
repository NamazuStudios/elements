package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import com.namazustudios.socialengine.service.gameon.client.invoker.*;
import com.namazustudios.socialengine.service.gameon.client.invoker.builder.*;

public class AppleIapReceiptInvokerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppleIapVerifyReceiptInvoker.Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);
    }
}
