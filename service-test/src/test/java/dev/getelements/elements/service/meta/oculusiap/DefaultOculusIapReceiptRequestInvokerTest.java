package dev.getelements.elements.service.meta.oculusiap;

import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;
import dev.getelements.elements.service.meta.oculusiap.invoker.DefaultOculusIapReceiptRequestInvoker;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

/**
 * Regression coverage for #101: Meta's own parsed {@code success:true} was being silently overwritten with
 * {@code false} because the invoker compared {@code response.getStatusInfo()} to {@code Response.Status.OK} by
 * reference identity. Jersey does not guarantee it hands back that exact enum singleton for a 200 response, so
 * the comparison could fail even on a genuinely successful call. These tests use a {@link Response.StatusType}
 * that is deliberately NOT the {@code Response.Status.OK} instance, mirroring what a real Jersey client can
 * return, to prove the fix compares the numeric status code instead.
 */
public class DefaultOculusIapReceiptRequestInvokerTest {

    private Client client;

    private WebTarget target;

    private Invocation.Builder requestBuilder;

    private Response response;

    private DefaultOculusIapReceiptRequestInvoker invoker;

    @BeforeMethod
    public void setup() {

        client = mock(Client.class);
        target = mock(WebTarget.class);
        requestBuilder = mock(Invocation.Builder.class);
        response = mock(Response.class);

        when(client.target(anyString())).thenReturn(target);
        when(target.path(anyString())).thenReturn(target);
        when(target.request(any(jakarta.ws.rs.core.MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.post(any())).thenReturn(response);

        invoker = new DefaultOculusIapReceiptRequestInvoker();
        invoker.setClient(client);

    }

    private static OculusIapReceipt receipt() {
        final var receipt = new OculusIapReceipt();
        receipt.setUserId("user-1");
        receipt.setSku("some.sku");
        return receipt;
    }

    // A distinct StatusType instance representing 200 -- NOT Response.Status.OK -- reproducing the exact
    // Jersey behavior that caused the original bug (reference-identity comparison failing on a real 200).
    private static Response.StatusType distinctOkStatusType() {
        return new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return 200;
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.Family.SUCCESSFUL;
            }

            @Override
            public String getReasonPhrase() {
                return "OK";
            }
        };
    }

    @Test
    public void testSuccessIsTrueOnRealTwoHundredEvenWithDistinctStatusTypeInstance() {

        when(response.getStatus()).thenReturn(200);
        when(response.getStatusInfo()).thenReturn(distinctOkStatusType());

        final var metaBody = new OculusIapVerifyReceiptResponse();
        metaBody.setSuccess(true);
        metaBody.setGrantTime(1789608618L);
        when(response.readEntity(OculusIapVerifyReceiptResponse.class)).thenReturn(metaBody);

        final var result = invoker.invokeVerify(receipt(), "app-1", "secret-1");

        assertTrue(result.isSuccess(), "A real 200 must report success, regardless of StatusType identity");

    }

    @Test
    public void testSuccessIsFalseOnNonTwoHundredStatus() {

        when(response.getStatus()).thenReturn(400);
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);

        final var metaBody = new OculusIapVerifyReceiptResponse();
        when(response.readEntity(OculusIapVerifyReceiptResponse.class)).thenReturn(metaBody);

        final var result = invoker.invokeVerify(receipt(), "app-1", "secret-1");

        assertTrue(!result.isSuccess());

    }

}
