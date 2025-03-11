package dev.getelements.elements.service.appleiap.client.invoker.invoker;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.appleiap.client.exception.AppleIapVerifyReceiptStatusErrorCodeException;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapVerifyReceiptResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static dev.getelements.elements.sdk.service.appleiap.AppleIapConstants.*;
import static dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.PRODUCTION;
import static dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.Response.Status.OK;

public class DefaultAppleIapVerifyReceiptInvoker implements AppleIapVerifyReceiptInvoker {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAppleIapVerifyReceiptInvoker.class);

    private final Client client;

    private final AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment;

    private final String receiptData;

    public DefaultAppleIapVerifyReceiptInvoker(
            final Client client,
            final AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment,
            final String receiptData) {
        this.client = client;
        this.appleIapVerifyReceiptEnvironment = appleIapVerifyReceiptEnvironment;
        this.receiptData = receiptData;
    }

    @Override
    public AppleIapGrandUnifiedReceipt invoke() {

        AppleIapVerifyReceiptResponse response;

        if (appleIapVerifyReceiptEnvironment == null) {

            response = doInvoke(PRODUCTION);

            if (response.getStatus() == USE_SANDBOX_INSTEAD) {
                response = doInvoke(SANDBOX);
            }

        } else {
            response = doInvoke(appleIapVerifyReceiptEnvironment);
        }

        final int status = response.getStatus();

        if (status != VALID_STATUS_CODE) {
            throw new AppleIapVerifyReceiptStatusErrorCodeException(status);
        }

        return response.getReceipt();

    }

    private AppleIapVerifyReceiptResponse doInvoke(final AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment) {

        final HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put(RECEIPT_DATA_KEY, this.receiptData);

        final String baseApi;

        switch (appleIapVerifyReceiptEnvironment) {
            case PRODUCTION:
                baseApi = PRODUCTION_BASE_API_URL;
                break;
            case SANDBOX:
                baseApi = SANDBOX_BASE_API_URL;
                break;
            default:
                throw new IllegalArgumentException("Invalid environment: " + appleIapVerifyReceiptEnvironment);
        }

        final Response response = client
                .target(baseApi)
                .path(VERIFY_RECEIPT_PATH_COMPONENT)
                .request(APPLICATION_JSON_TYPE)
                .post(entity(requestBody, APPLICATION_JSON_TYPE));

        if (OK.getStatusCode() != response.getStatus()) {
            logger.error("Apple {} server returned error status {}", appleIapVerifyReceiptEnvironment, response.getStatus());
            throw new InternalException("Failed to make API call with Apple IAP Receipt Verification servers: " + response.getStatus());
        }

        final AppleIapVerifyReceiptResponse appleIapVerifyReceiptResponse;
        appleIapVerifyReceiptResponse = response.readEntity(AppleIapVerifyReceiptResponse.class);

        return appleIapVerifyReceiptResponse;

    }

}
