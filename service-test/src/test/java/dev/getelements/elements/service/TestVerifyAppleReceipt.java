package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static com.google.common.io.ByteStreams.toByteArray;
import static dev.getelements.elements.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;

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
            install(new JerseyHttpClientModule());
        }

    }

}
