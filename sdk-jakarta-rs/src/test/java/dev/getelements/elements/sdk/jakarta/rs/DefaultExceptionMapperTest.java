package dev.getelements.elements.sdk.jakarta.rs;

import dev.getelements.elements.sdk.model.ErrorResponse;
import dev.getelements.elements.sdk.model.exception.ErrorCode;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.exception.auth.IdentityAlreadyLinkedException;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DefaultExceptionMapperTest {

    /**
     * The dedicated identity-already-linked code must map to HTTP 409 CONFLICT, not the generic 403.
     */
    @Test
    public void identityAlreadyLinked_mapsToConflictStatus() {
        assertEquals(DefaultExceptionMapper.HTTP_STATUS_MAP.get(ErrorCode.IDENTITY_ALREADY_LINKED),
                Response.Status.CONFLICT);
        assertEquals(DefaultExceptionMapper.getStatusForCode(ErrorCode.IDENTITY_ALREADY_LINKED),
                Response.Status.CONFLICT);
    }

    /**
     * The mapped response must carry the dedicated code in the body so clients can detect the collision
     * programmatically without parsing the message.
     */
    @Test
    public void identityAlreadyLinked_responseCarriesDedicatedCode() {
        final var mapper = new DefaultExceptionMapper();
        final var response = mapper.toResponse(
                new IdentityAlreadyLinkedException("External OIDC identity is already linked to a different user."));

        assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode());

        final var body = (ErrorResponse) response.getEntity();
        assertEquals(body.getCode(), ErrorCode.IDENTITY_ALREADY_LINKED.toString());
        assertNotNull(body.getMessage());
    }

    /**
     * Existing handlers catching AuthValidationException must keep working.
     */
    @Test
    public void identityAlreadyLinked_remainsAuthValidationException() {
        final var ex = new IdentityAlreadyLinkedException("x");
        assertTrue(ex instanceof AuthValidationException);
    }

}
