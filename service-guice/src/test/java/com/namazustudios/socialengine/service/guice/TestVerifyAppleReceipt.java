package com.namazustudios.socialengine.service.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.service.appleiap.client.exception.AppleIapVerifyReceiptStatusErrorCodeException;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.charset.Charset;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.SNAKE;
import static com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;
import static org.testng.Assert.fail;

@Guice(modules = TestVerifyAppleReceipt.Module.class)
public class TestVerifyAppleReceipt {

    private AppleIapVerifyReceiptInvoker.Builder builder;

    @Test
    public void testSandboxVerification() throws Exception {
        try (final InputStream is = TestVerifyAppleReceipt.class.getResourceAsStream("/iap_sandbox_receipt.txt")) {

            final String receiptData = new String(ByteStreams.toByteArray(is), Charset.forName("UTF-8"));

            getBuilder()
                .withEnvironment(SANDBOX)
                .withReceiptData(receiptData)
                .build()
                .invoke();

            fail("Expected exception.");

        } catch (final AppleIapVerifyReceiptStatusErrorCodeException ex) {
            // Pass Test
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
            }).withNamedObjectMapperProvider(SNAKE, () -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            }));
        }

    }

}
