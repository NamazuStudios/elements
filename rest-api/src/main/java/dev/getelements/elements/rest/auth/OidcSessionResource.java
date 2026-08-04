package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptRequest;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptResponse;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptState;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provider-agnostic browser-redirect OIDC login for thick clients that cannot receive a browser redirect
 * directly. See {@link OidcCallbackResource} for the provider-facing half of the flow.
 */
@Path("oidc/session")
public class OidcSessionResource {

    private ValidationHelper validationHelper;

    private OidcLoginAttemptService oidcLoginAttemptService;

    private OidcAuthService oidcAuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Begins an OIDC login attempt, or validates a possessed id_token directly",
            description = "Supplying only 'provider' starts a pending browser-redirect login attempt and " +
                    "returns a handle plus the authorize URL to open in the system browser. Additionally " +
                    "supplying 'idToken' instead validates it directly and returns a completed session " +
                    "synchronously, sharing validation code with the callback path.")
    public Response createOidcSession(final OidcLoginAttemptRequest request) {

        getValidationHelper().validateModel(request);

        if (!isNullOrEmpty(request.getIdToken())) {

            final var oidcSessionRequest = new OidcSessionRequest();
            oidcSessionRequest.setJwt(request.getIdToken());

            final var sessionCreation = getOidcAuthService().createSession(oidcSessionRequest);
            final var body = OidcLoginAttemptResponse.complete(sessionCreation);

            return Response.ok(body).build();

        }

        final var begin = getOidcLoginAttemptService().begin(
                request.getProvider(), request.getSuccessRedirectUrl(), request.getErrorRedirectUrl());
        final var body = OidcLoginAttemptResponse.pending(begin.getHandle(), begin.getAuthorizeUrl(), begin.getExpiresAt());

        return Response.status(Response.Status.CREATED).entity(body).build();

    }

    @GET
    @Path("{handle}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Polls a pending OIDC login attempt",
            description = "Returns COMPLETE with the session exactly once, on the poll that first observes " +
                    "completion; a subsequent poll for the same handle, or a poll for an unknown/expired " +
                    "handle, returns 404.")
    public OidcLoginAttemptStatusResponse pollOidcSession(@PathParam("handle") final String handle) {

        final var status = getOidcLoginAttemptService().poll(handle);

        if (status.getStatus() == OidcLoginAttemptState.EXPIRED) {
            throw new NotFoundException();
        }

        return status;

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public OidcLoginAttemptService getOidcLoginAttemptService() {
        return oidcLoginAttemptService;
    }

    @Inject
    public void setOidcLoginAttemptService(OidcLoginAttemptService oidcLoginAttemptService) {
        this.oidcLoginAttemptService = oidcLoginAttemptService;
    }

    public OidcAuthService getOidcAuthService() {
        return oidcAuthService;
    }

    @Inject
    public void setOidcAuthService(OidcAuthService oidcAuthService) {
        this.oidcAuthService = oidcAuthService;
    }

}
