package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.*;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapVerifyReceiptResponse;
import dev.getelements.elements.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static com.google.common.io.ByteStreams.toByteArray;
import static dev.getelements.elements.sdk.service.appleiap.AppleIapConstants.*;
import static dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Guice(modules = TestVerifyAppleReceipt.Module.class)
public class TestVerifyAppleReceipt {

    private Builder builder;

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

    public Builder getBuilder() {
        return builder;
    }

    @Inject
    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);
        }

        @Provides
        Client provideClient() {

            final AppleIapVerifyReceiptResponse sandboxApiResponse = new AppleIapVerifyReceiptResponse();
            sandboxApiResponse.setStatus(VALID_STATUS_CODE);
            sandboxApiResponse.setReceipt(new AppleIapGrandUnifiedReceipt());

            final Response sandboxHttpResponse = mock(Response.class);
            when(sandboxHttpResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(sandboxHttpResponse.readEntity(AppleIapVerifyReceiptResponse.class)).thenReturn(sandboxApiResponse);

            final Invocation.Builder sandboxRequestBuilder = mock(Invocation.Builder.class);
            when(sandboxRequestBuilder.post(any())).thenReturn(sandboxHttpResponse);

            final WebTarget sandboxPathTarget = mock(WebTarget.class);
            when(sandboxPathTarget.request(APPLICATION_JSON_TYPE)).thenReturn(sandboxRequestBuilder);

            final WebTarget sandboxBaseTarget = mock(WebTarget.class);
            when(sandboxBaseTarget.path(VERIFY_RECEIPT_PATH_COMPONENT)).thenReturn(sandboxPathTarget);

            final AppleIapVerifyReceiptResponse productionApiResponse = new AppleIapVerifyReceiptResponse();
            productionApiResponse.setStatus(USE_SANDBOX_INSTEAD);

            final Response productionHttpResponse = mock(Response.class);
            when(productionHttpResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(productionHttpResponse.readEntity(AppleIapVerifyReceiptResponse.class)).thenReturn(productionApiResponse);

            final Invocation.Builder productionRequestBuilder = mock(Invocation.Builder.class);
            when(productionRequestBuilder.post(any())).thenReturn(productionHttpResponse);

            final WebTarget productionPathTarget = mock(WebTarget.class);
            when(productionPathTarget.request(APPLICATION_JSON_TYPE)).thenReturn(productionRequestBuilder);

            final WebTarget productionBaseTarget = mock(WebTarget.class);
            when(productionBaseTarget.path(VERIFY_RECEIPT_PATH_COMPONENT)).thenReturn(productionPathTarget);

            final Client client = mock(Client.class);
            when(client.target(SANDBOX_BASE_API_URL)).thenReturn(sandboxBaseTarget);
            when(client.target(PRODUCTION_BASE_API_URL)).thenReturn(productionBaseTarget);

            return client;

        }

    }

}
