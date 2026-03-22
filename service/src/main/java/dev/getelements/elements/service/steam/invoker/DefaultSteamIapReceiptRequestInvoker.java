package dev.getelements.elements.service.steam.invoker;

import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;

public class DefaultSteamIapReceiptRequestInvoker implements SteamIapReceiptRequestInvoker {

    private static final String STEAM_MICRO_TXN_ROOT_URL = "https://partner.steam-api.com";

    private Client client;

    @Override
    public SteamIapQueryTxnResponse invokeQueryTxn(
            final String publisherKey,
            final String appId,
            final String orderId
    ) {
        return getClient()
                .target(STEAM_MICRO_TXN_ROOT_URL)
                .path("ISteamMicroTxn")
                .path("QueryTxn")
                .path("v3")
                .queryParam("key", publisherKey)
                .queryParam("appid", appId)
                .queryParam("orderid", orderId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SteamIapQueryTxnResponse.class);
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
