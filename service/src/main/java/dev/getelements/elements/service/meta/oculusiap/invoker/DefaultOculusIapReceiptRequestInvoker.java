package dev.getelements.elements.service.meta.oculusiap.invoker;

import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.exception.OculusIapVerifyReceiptGraphErrorException;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker.OculusIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

        final var response = client.target(OCULUS_IAP_ROOT_URL)
                .path(appId)
                .path("verify_entitlement")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(entity);

        try(response) {
            final var success = response.getStatusInfo() == Response.Status.OK;
            final var responseEntity = response.readEntity(OculusIapVerifyReceiptResponse.class);
            responseEntity.setSuccess(success);
            return responseEntity;
        }  catch (Exception e) {
            throw new OculusIapVerifyReceiptGraphErrorException(response.readEntity(String.class));
        }
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
        final var response = client.target(OCULUS_IAP_ROOT_URL)
                .path(appId)
                .path("consume_entitlement")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(entity);

        try(response) {

            return response.readEntity(OculusIapConsumeResponse.class);
        } catch (Exception e) {
            throw new OculusIapVerifyReceiptGraphErrorException(response.readEntity(String.class));
        }
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
