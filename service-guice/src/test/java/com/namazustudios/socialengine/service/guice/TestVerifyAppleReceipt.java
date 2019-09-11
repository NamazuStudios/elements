package com.namazustudios.socialengine.service.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import com.namazustudios.socialengine.util.AppleDateFormat;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;
import static com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;

@Guice(modules = TestVerifyAppleReceipt.Module.class)
public class TestVerifyAppleReceipt {

    private AppleIapVerifyReceiptInvoker.Builder builder;

    @Test
    public void testSandboxVerification() throws Exception {
        try (final InputStream is = TestVerifyAppleReceipt.class.getResourceAsStream("/iap_sandbox_receipt.txt")) {

            final String receiptData = new String(toByteArray(is), "UTF-8");

            getBuilder()
                .withEnvironment(SANDBOX)
                .withReceiptData(receiptData)
                .build()
                .invoke();

        }
    }

    @Test
    public void testDefaultVerification() throws Exception {
        try (final InputStream is = TestVerifyAppleReceipt.class.getResourceAsStream("/iap_sandbox_receipt.txt")) {

            final String receiptData = new String(toByteArray(is), "UTF-8");

            getBuilder()
                    .withReceiptData(receiptData)
                    .build()
                    .invoke();

        }
    }

    public AppleIapVerifyReceiptInvoker.Builder getBuilder() {
        return builder;
    }

    @Inject
    public void setBuilder(AppleIapVerifyReceiptInvoker.Builder builder) {
        this.builder = builder;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(AppleIapVerifyReceiptInvoker.Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);
            install(new JacksonHttpClientModule()
            .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
            .withDefaultObjectMapperProvider(() -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            }).withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setDateFormat(new AppleDateFormat());
                objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            }));
        }

    }

}
