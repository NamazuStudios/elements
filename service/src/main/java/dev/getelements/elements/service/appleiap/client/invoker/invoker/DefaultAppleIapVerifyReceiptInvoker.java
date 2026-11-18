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

            if (Integer.valueOf(USE_SANDBOX_INSTEAD).equals(response.getStatus())) {
                response = doInvoke(SANDBOX);
            }

        } else {
            response = doInvoke(appleIapVerifyReceiptEnvironment);
        }

        if (response.getStatus() == null) {
            throw new InternalException("Apple IAP receipt verification response did not contain a status.");
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

        Response response = null;

        try {

            response = client
                    .target(baseApi)
                    .path(VERIFY_RECEIPT_PATH_COMPONENT)
                    .request(APPLICATION_JSON_TYPE)
                    .post(entity(requestBody, APPLICATION_JSON_TYPE));

            if (OK.getStatusCode() != response.getStatus()) {

                // Apple's non-200 bodies carry diagnostics (e.g. the shared secret mismatch detail).
                // Read the body before the response closes and surface it in the exception, so a log
                // names the failure rather than a bare status (#209-family hardening).
                String body;
                try {
                    body = response.readEntity(String.class);
                } catch (Exception ex) {
                    body = "<unreadable response body: " + ex.getMessage() + ">";
                }

                logger.error("Apple {} server returned error status {} body {}",
                        appleIapVerifyReceiptEnvironment, response.getStatus(), body);

                throw new InternalException("Failed to make API call with Apple IAP Receipt Verification servers: "
                        + response.getStatus() + " -- " + body);

            }

            return response.readEntity(AppleIapVerifyReceiptResponse.class);

        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

}
