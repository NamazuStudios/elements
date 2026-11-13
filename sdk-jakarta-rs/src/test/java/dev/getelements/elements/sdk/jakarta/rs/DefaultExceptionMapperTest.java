package dev.getelements.elements.sdk.jakarta.rs;

import dev.getelements.elements.sdk.model.ErrorResponse;
import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ErrorCode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DefaultExceptionMapperTest {

    /**
     * A failure reported by an external resource (e.g. Apple's verifyReceipt endpoint rejecting a
     * receipt) must map to 502 BAD_GATEWAY. Previously EXTERNAL_RESOURCE_FAILED was absent from the
     * status map and fell back to 500, making client-supplied data look like a server fault. Also,
     * the BaseException handler logged expected 500s at a level that appeared in no server logs.
     */
    @Test
    public void externalResourceFailed_mapsToBadGateway() {
        assertEquals(DefaultExceptionMapper.getStatusForCode(ErrorCode.EXTERNAL_RESOURCE_FAILED),
                Response.Status.BAD_GATEWAY);
    }

    /**
     * The mapped response must carry the dedicated code in the body so clients can distinguish an
     * upstream rejection from an internal fault without parsing the message.
     */
    @Test
    public void externalResourceFailed_responseCarriesDedicatedCode() {
        final var mapper = new DefaultExceptionMapper();
        final var response = mapper.toResponse(new BaseException("External resource failed.") {
            @Override
            public ErrorCode getCode() {
                return ErrorCode.EXTERNAL_RESOURCE_FAILED;
            }
        });

        assertEquals(response.getStatus(), Response.Status.BAD_GATEWAY.getStatusCode());

        final var body = (ErrorResponse) response.getEntity();
        assertEquals(body.getCode(), ErrorCode.EXTERNAL_RESOURCE_FAILED.toString());
        assertNotNull(body.getMessage());
    }

    /**
     * WebApplicationException responses must pass through their embedded status unchanged, while
     * still producing a JSON error body.
     */
    @Test
    public void webApplicationException_preservesStatusAndCarriesBody() {
        final var mapper = new DefaultExceptionMapper();
        final var response = mapper.toResponse(
                new WebApplicationException("Gone.", Response.Status.GONE));

        assertEquals(response.getStatus(), Response.Status.GONE.getStatusCode());

        final var body = (ErrorResponse) response.getEntity();
        assertEquals(body.getCode(), ErrorCode.UNKNOWN.toString());
        assertNotNull(body.getMessage());
    }

}
