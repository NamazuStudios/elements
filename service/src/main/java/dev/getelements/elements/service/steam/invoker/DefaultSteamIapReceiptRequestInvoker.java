package dev.getelements.elements.service.steam.invoker;

import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

        final var response = getClient()
                .target(STEAM_MICRO_TXN_ROOT_URL)
                .path(microTxn)
                .path("QueryTxn")
                .path("v3")
                .queryParam("key", publisherKey)
                .queryParam("appid", appId)
                .queryParam("orderid", orderId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {

            // Steam's error body (response.error.*) names the actual failure -- an order not found in
            // this environment, a key without access to the app, an app not entitled for micro-transactions
            // -- without which a bare HTTP 400/403 is undiagnosable (sandbox-vs-live mismatches present
            // exactly that way). Surface the queried endpoint, the status, and Steam's response body.
            String body;
            try {
                body = response.readEntity(String.class);
            } catch (Exception ex) {
                body = "<unreadable response body: " + ex.getMessage() + ">";
            }

            throw new SteamIapQueryTxnException(microTxn
                    + "/QueryTxn/v3 returned HTTP " + response.getStatus()
                    + " for appid " + appId + " orderid " + orderId + ": " + body);

        }

        return response.readEntity(SteamIapQueryTxnResponse.class);
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
