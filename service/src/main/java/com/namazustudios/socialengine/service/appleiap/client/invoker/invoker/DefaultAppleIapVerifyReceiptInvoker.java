package com.namazustudios.socialengine.service.appleiap.client.invoker.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.service.appleiap.client.exception.AppleIapVerifyReceiptStatusErrorCodeException;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapVerifyReceiptResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.namazustudios.socialengine.AppleIapConstants.*;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;

public class DefaultAppleIapVerifyReceiptInvoker implements AppleIapVerifyReceiptInvoker {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAppleIapVerifyReceiptInvoker.class);

    private final Client client;

    private final AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment;

    private final String receiptData;

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
        objectMapper.setDateFormat(simpleDateFormat);
    }

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
        final HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put(RECEIPT_DATA_KEY, this.receiptData);

        final String baseApi;

        switch (appleIapVerifyReceiptEnvironment) {
            case PRODUCTION:
                baseApi = PRODUCTION_BASE_API_URL;
                break;
            case SANDBOX:
            default:
                baseApi = SANDBOX_BASE_API_URL;
                break;
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

        final String respString = response.readEntity(String.class);

        final AppleIapVerifyReceiptResponse appleIapVerifyReceiptResponse;

        try {
            appleIapVerifyReceiptResponse =
                    getObjectMapper().readValue(respString, AppleIapVerifyReceiptResponse.class);
        }
        catch (IOException e) {
            throw new InternalException("Failed to parse Apple response.");
        }

        final int status = appleIapVerifyReceiptResponse.getStatus();
        if (status != VALID_STATUS_CODE) {
            throw new AppleIapVerifyReceiptStatusErrorCodeException(status);
        }

        return appleIapVerifyReceiptResponse.getReceipt();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
