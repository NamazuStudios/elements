package dev.getelements.elements.service.receipt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Verifies that the Steam invoker targets the correct ISteamMicroTxn environment: the live
 * ISteamMicroTxn endpoint by default, and ISteamMicroTxnSandbox when the application configuration
 * enables sandbox verification (#222 -- development/team purchases are recorded only in the
 * sandbox).
 */
public class SteamIapReceiptRequestInvokerTest {

    private Client client;

    private WebTarget webTarget;

    private Invocation.Builder requestBuilder;

    private dev.getelements.elements.service.steam.invoker.DefaultSteamIapReceiptRequestInvoker invoker;

    @BeforeMethod
    public void setUp() {

        client = mock(Client.class);
        webTarget = mock(WebTarget.class);
        requestBuilder = mock(Invocation.Builder.class);

        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.queryParam(anyString(), any())).thenReturn(webTarget);
        when(webTarget.request(eq(MediaType.APPLICATION_JSON_TYPE))).thenReturn(requestBuilder);
        when(requestBuilder.get(SteamIapQueryTxnResponse.class)).thenReturn(new SteamIapQueryTxnResponse());

        invoker = new dev.getelements.elements.service.steam.invoker.DefaultSteamIapReceiptRequestInvoker();
        invoker.setClient(client);

    }

    @Test
    public void sandboxFlagTargetsISteamMicroTxnSandbox() {

        invoker.invokeQueryTxn("publisher-key", "3855060", "order-1", true);

        final var pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(webTarget, org.mockito.Mockito.atLeastOnce()).path(pathCaptor.capture());
        assertTrue(pathCaptor.getAllValues().contains("ISteamMicroTxnSandbox"),
                "expected the sandbox endpoint, paths were: " + pathCaptor.getAllValues());
        assertTrue(!pathCaptor.getAllValues().contains("ISteamMicroTxn"),
                "the live endpoint must not be queried in sandbox mode");

    }

    @Test
    public void liveFlagTargetsISteamMicroTxn() {

        invoker.invokeQueryTxn("publisher-key", "3855060", "order-1", false);

        final var pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(webTarget, org.mockito.Mockito.atLeastOnce()).path(pathCaptor.capture());
        assertTrue(pathCaptor.getAllValues().contains("ISteamMicroTxn"),
                "expected the live endpoint, paths were: " + pathCaptor.getAllValues());
        assertTrue(!pathCaptor.getAllValues().contains("ISteamMicroTxnSandbox"),
                "the sandbox endpoint must not be queried in live mode");

    }

}
