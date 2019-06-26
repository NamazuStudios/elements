package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;

public class AppleIapReceiptInvokerModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(AppleIapVerifyReceiptInvoker.Builder.class);

        bind(AppleIapVerifyReceiptInvoker.Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);

        install(new JacksonHttpClientModule().withObjectMapperProvider(() -> {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
            return objectMapper;
        }));

    }

}
