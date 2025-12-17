package dev.getelements.elements.service.meta.facebookiap.invoker;

import dev.getelements.elements.sdk.model.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.invoker.FacebookIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapVerifyReceiptResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService.OCULUS_ROOT_URL;

public class DefaultFacebookIapReceiptRequestInvoker implements FacebookIapReceiptRequestInvoker {

    private Client client;

    @Override
    public FacebookIapVerifyReceiptResponse invokeVerify(FacebookIapReceipt receipt, String appId, String appSecret) {

        final var userId = receipt.getFbUserId();
        final var sku = receipt.getSku();
        final var accessToken = createAccessToken(appId, appSecret);
        final var form = new Form()
                .param("access_token", accessToken)
                .param("user_id", userId)
                .param("sku", sku);

        final var entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        return client.target(OCULUS_ROOT_URL)
                .path(appId)
                .path("verify_entitlement")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(entity, FacebookIapVerifyReceiptResponse.class);
    }

    @Override
    public FacebookIapConsumeResponse invokeConsume(FacebookIapReceipt receipt, String appId, String appSecret) {

        final var userId = receipt.getFbUserId();
        final var sku = receipt.getSku();
        final var accessToken = createAccessToken(appId, appSecret);

        final var form = new Form()
                .param("access_token", accessToken)
                .param("user_id", userId)
                .param("sku", sku);

        final var entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        return client.target(OCULUS_ROOT_URL)
                .path(appId)
                .path("consume_entitlement")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(entity, FacebookIapConsumeResponse.class);
    }

    private String createAccessToken(String appId, String appSecret) {
        return "OC|" + appId + "|" + appSecret;
    }



    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
