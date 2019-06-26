package com.namazustudios.socialengine.service.appleiap.client.invoker.builder;

import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.Builder;
import com.namazustudios.socialengine.service.appleiap.client.invoker.invoker.DefaultAppleIapVerifyReceiptInvoker;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

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
        if (appleIapVerifyReceiptEnvironment == null) {
            throw new IllegalStateException("appleIapVerifyReceiptEnvironment is null.");
        }

        if (receiptData == null) {
            throw new IllegalStateException("receiptData is null.");
        }

        if (receiptData.trim().length() == 0) {
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

