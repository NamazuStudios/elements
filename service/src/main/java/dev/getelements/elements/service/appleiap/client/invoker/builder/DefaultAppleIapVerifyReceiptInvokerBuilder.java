package dev.getelements.elements.service.appleiap.client.invoker.builder;

import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.*;
import dev.getelements.elements.service.appleiap.client.invoker.invoker.DefaultAppleIapVerifyReceiptInvoker;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;

public class DefaultAppleIapVerifyReceiptInvokerBuilder implements Builder {

    private Client client;

    private AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment;

    private String receiptData;

    @Override
    public Builder withEnvironment(AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment) {
        this.appleIapVerifyReceiptEnvironment = appleIapVerifyReceiptEnvironment;
        return this;
    }

    @Override
    public Builder withReceiptData(String receiptData) {
        this.receiptData = receiptData;
        return this;
    }

    @Override
    public AppleIapVerifyReceiptInvoker build() {

        if (receiptData == null) {
            throw new IllegalStateException("receiptData is null.");
        }

        if (receiptData.trim().isEmpty()) {
            throw new IllegalStateException("receiptData is empty.");
        }

        return new DefaultAppleIapVerifyReceiptInvoker(getClient(), appleIapVerifyReceiptEnvironment, receiptData);
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}

