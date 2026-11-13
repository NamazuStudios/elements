package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.exception.ErrorCode;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.appleiap.client.exception.AppleIapVerifyReceiptStatusErrorCodeException;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapVerifyReceiptResponse;
import dev.getelements.elements.service.appleiap.client.invoker.invoker.DefaultAppleIapVerifyReceiptInvoker;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Regression tests for the Apple verifyReceipt invocation path. A receipt rejected by Apple must
 * surface as a coded, logged exception rather than a generic 500 with no server-side log, and a
 * 200 response missing the "status" field must not trip an NPE.
 */
public class AppleIapVerifyReceiptInvokerTest {

    private Client client;

    private WebTarget webTarget;

    private Invocation.Builder requestBuilder;

    private Response httpResponse;

    @BeforeMethod
    public void setUp() {

        client = mock(Client.class);
        webTarget = mock(WebTarget.class);
        requestBuilder = mock(Invocation.Builder.class);
        httpResponse = mock(Response.class);

        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.request(any(MediaType[].class))).thenReturn(requestBuilder);
        when(requestBuilder.post(any(Entity.class))).thenReturn(httpResponse);

    }

    /**
     * A 200 response from Apple with a non-zero "status" (e.g. 21003, invalid receipt) must throw
     * AppleIapVerifyReceiptStatusErrorCodeException, preserving Apple's status code. Previously the
     * resulting error code was unmapped in the REST layer, producing a bare 500 with no logs.
     */
    @Test
    public void testRejectedReceiptThrowsAppleIapVerifyReceiptStatusErrorCodeException() {

        final var appleIapVerifyReceiptResponse = new AppleIapVerifyReceiptResponse();
        appleIapVerifyReceiptResponse.setStatus(21003);

        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.readEntity(AppleIapVerifyReceiptResponse.class))
                .thenReturn(appleIapVerifyReceiptResponse);

        final var invoker = new DefaultAppleIapVerifyReceiptInvoker(client, SANDBOX, "receipt-data");

        try {
            invoker.invoke();
            fail("Expected AppleIapVerifyReceiptStatusErrorCodeException");
        } catch (AppleIapVerifyReceiptStatusErrorCodeException e) {
            assertEquals(e.getStatusCode(), 21003);
            assertEquals(e.getCode(), ErrorCode.EXTERNAL_RESOURCE_FAILED);
            assertNotNull(e.getMessage());
        }

    }

    /**
     * A 200 response body without a "status" field must produce an InternalException with a useful
     * message. Previously the Integer unboxing NPE'd, surfacing as a generic 500.
     */
    @Test
    public void testMissingStatusThrowsInternalExceptionNotNpe() {

        final var appleIapVerifyReceiptResponse = new AppleIapVerifyReceiptResponse();
        // "status" deliberately left null.

        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.readEntity(AppleIapVerifyReceiptResponse.class))
                .thenReturn(appleIapVerifyReceiptResponse);

        final var invoker = new DefaultAppleIapVerifyReceiptInvoker(client, SANDBOX, "receipt-data");

        try {
            invoker.invoke();
            fail("Expected InternalException");
        } catch (InternalException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * The production-then-sandbox retry on status 21007 must still work: the first (production)
     * call returns 21007, the second (sandbox) call succeeds.
     */
    @Test
    public void testProductionToSandboxRetryOn21007() {

        final var useSandboxResponse = new AppleIapVerifyReceiptResponse();
        useSandboxResponse.setStatus(21007);

        final var okResponse = new AppleIapVerifyReceiptResponse();
        okResponse.setStatus(0);
        okResponse.setReceipt(new AppleIapGrandUnifiedReceipt());

        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.readEntity(AppleIapVerifyReceiptResponse.class))
                .thenReturn(useSandboxResponse, okResponse);

        final var invoker = new DefaultAppleIapVerifyReceiptInvoker(client, null, "receipt-data");

        final var result = invoker.invoke();

        assertNotNull(result);

    }

    /**
     * A non-200 HTTP response from Apple must throw InternalException (never a bare 500 without a
     * log -- the invoker logs the upstream status before throwing).
     */
    @Test
    public void testNonOkHttpResponseThrowsInternalException() {

        when(httpResponse.getStatus()).thenReturn(503);

        final var invoker = new DefaultAppleIapVerifyReceiptInvoker(client, SANDBOX, "receipt-data");

        try {
            invoker.invoke();
            fail("Expected InternalException");
        } catch (InternalException e) {
            assertNotNull(e.getMessage());
        }

    }

}
