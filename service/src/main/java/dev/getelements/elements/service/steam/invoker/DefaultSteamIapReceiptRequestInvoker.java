package dev.getelements.elements.service.steam.invoker;

import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;

public class DefaultSteamIapReceiptRequestInvoker implements SteamIapReceiptRequestInvoker {

    private static final String STEAM_MICRO_TXN_ROOT_URL = "https://partner.steam-api.com";

    private static final String STEAM_MICRO_TXN = "ISteamMicroTxn";

    private static final String STEAM_MICRO_TXN_SANDBOX = "ISteamMicroTxnSandbox";

    private Client client;

    @Override
    public SteamIapQueryTxnResponse invokeQueryTxn(
            final String publisherKey,
            final String appId,
            final String orderId,
            final boolean sandbox
    ) {
        // Team/development purchases are recorded only in the sandbox environment (they do not
        // appear in the live ISteamMicroTxn endpoint until the application is released), so the
        // environment is a configuration property of the application.
        final var microTxn = sandbox ? STEAM_MICRO_TXN_SANDBOX : STEAM_MICRO_TXN;

        return getClient()
                .target(STEAM_MICRO_TXN_ROOT_URL)
                .path(microTxn)
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
