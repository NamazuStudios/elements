package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.SnakeCaseMapperProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder.JAXRS_CLIENT;
import static javax.ws.rs.client.ClientBuilder.*;

public class AppleIapReceiptInvokerModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(AppleIapVerifyReceiptInvoker.Builder.class);
        bind(AppleIapVerifyReceiptInvoker.Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);

        bind(Client.class).toProvider(() -> {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            return newBuilder().register(objectMapper).build();
        }).asEagerSingleton();

    }

}
