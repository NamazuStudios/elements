package dev.getelements.elements.service.meta.oculusiap.invoker;

import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker.OculusIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService.OCULUS_IAP_ROOT_URL;

public class DefaultOculusIapReceiptRequestInvoker implements OculusIapReceiptRequestInvoker {

    private Client client;

    @Override
    public OculusIapVerifyReceiptResponse invokeVerify(OculusIapReceipt receipt, String appId, String appSecret) {

        final var userId = receipt.getUserId();
        final var sku = receipt.getSku();
        final var accessToken = createAccessToken(appId, appSecret);
        final var form = new Form()
                .param("access_token", accessToken)
                .param("user_id", userId)
                .param("sku", sku);

        final var entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        return client.target(OCULUS_IAP_ROOT_URL)
                .path(appId)
                .path("verify_entitlement")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(entity, OculusIapVerifyReceiptResponse.class);
    }

    @Override
    public OculusIapConsumeResponse invokeConsume(OculusIapReceipt receipt, String appId, String appSecret) {

        final var userId = receipt.getUserId();
        final var sku = receipt.getSku();
        final var accessToken = createAccessToken(appId, appSecret);

        final var form = new Form()
                .param("access_token", accessToken)
                .param("user_id", userId)
                .param("sku", sku)
                .param("purchase_id", receipt.getPurchaseId());

        final var entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        return client.target(OCULUS_IAP_ROOT_URL)
                .path(appId)
                .path("consume_entitlement")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(entity, OculusIapConsumeResponse.class);
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
